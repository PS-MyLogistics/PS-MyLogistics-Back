package com.mylogisticcba.iam.security.auth.services.impls;

import com.mylogisticcba.common.rateLimit.RateLimitService;
import com.mylogisticcba.common.rateLimit.RateLimitType;
import com.mylogisticcba.iam.events.ResetPasswordConfirmEvent;
import com.mylogisticcba.iam.events.ResetPasswordConfirmLimitRequestExceededEvent;
import com.mylogisticcba.iam.events.ResetPasswordRequestTokenExceededEvent;
import com.mylogisticcba.iam.events.ResetPasswordTokenCreatedEvent;
import com.mylogisticcba.iam.repositories.ResetPasswordRepository;
import com.mylogisticcba.iam.repositories.UserRepository;
import com.mylogisticcba.iam.security.auth.dtos.req.ConfirmResetPassRequest;
import com.mylogisticcba.iam.security.auth.dtos.req.SoliciteResetPassRequest;
import com.mylogisticcba.iam.security.auth.dtos.response.EncodedMailResponse;
import com.mylogisticcba.iam.security.auth.entity.ResetPasswordToken;
import com.mylogisticcba.iam.security.auth.exceptions.AuthServiceException;
import com.mylogisticcba.iam.security.auth.exceptions.ResetPasswordServiceException;
import com.mylogisticcba.iam.security.auth.securityCustoms.CustomUserDetails;
import com.mylogisticcba.iam.security.jwt.JwtService;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.iam.tenant.enums.UserStatus;
import com.mylogisticcba.iam.tenant.exceptions.TenantServiceException;
import com.mylogisticcba.iam.tenant.exceptions.UserServiceException;
import com.mylogisticcba.iam.tenant.services.impl.TenantService;
import com.mylogisticcba.iam.tenant.services.impl.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordService {

    private final ResetPasswordRepository resetPasswordRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final TenantService tenantService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final RateLimitService rateLimitService;

    //TODO: se puede llevar a configurable por tenant o global app.propeerties
    private final RateLimitType RESET_REQUEST_LIMIT = RateLimitType.of("RESET_REQUEST", "Password Reset Request");
    private final RateLimitType RESET_CONFIRM_LIMIT = RateLimitType.of("RESET_CONFIRM", "Password Reset Confirmation");
    private final Integer MAX_REQUEST_RESET_ATTEMPTS = 5;
    private Integer MAX_CONFIRM_RESET_ATTEMPTS = 5;

    public EncodedMailResponse getMaskEmail(String username, String tenantName) {

        TenantEntity tenant = tenantService.getTenantByName(tenantName);
        UserEntity user = userService.getUserByUsernameAndTenant(username, tenant.getId());

        EncodedMailResponse rsta = new EncodedMailResponse();
        rsta.setSuccess(true);
        rsta.setMailEncoded(maskEmail(user.getEmail()));
        return rsta;
    }


    @Transactional
    public void requestResetToken(SoliciteResetPassRequest soliciteResetPassRequest) {

        String key = soliciteResetPassRequest.getUsername()  + ":"+ soliciteResetPassRequest.getTenantName();
        // Verificar si ya superó el límite
        if (rateLimitService.isLimitExceeded(RESET_REQUEST_LIMIT, key, MAX_REQUEST_RESET_ATTEMPTS)) {

            notificateRequestResetLimitExceeded(soliciteResetPassRequest.getUsername(),soliciteResetPassRequest.getTenantName());
                throw new ResetPasswordServiceException("Too many reset requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        }
        try {
            TenantEntity tenant = tenantService.getTenantByName(soliciteResetPassRequest.getTenantName());
            UserEntity user = userService.getUserByUsernameAndEmailAndTenantId(
                    soliciteResetPassRequest.getUsername(),
                    soliciteResetPassRequest.getEmail(),
                    tenant.getId());

            if (!user.getStatus().equals(UserStatus.ACTIVE)) {
                rateLimitService.recordFailedAttempt(RESET_REQUEST_LIMIT, key, Duration.ofMinutes(20));
                throw new ResetPasswordServiceException("User is not active");
            }

            // Generate jwt and pesist his hash in entity tokenResetPassword
            String jwtResetToken = jwtService.getToken(new CustomUserDetails(user, null));
            String jtwTokenHashed = passwordEncoder.encode(jwtResetToken);
            ResetPasswordToken resetPassTokenEntity = ResetPasswordToken.builder().id(UUID.randomUUID())
                    .tokenHash(jtwTokenHashed)
                    .isRevoked(false)
                    .isUsed(false)
                    .tenantId(tenant.getId())
                    .userId(user.getId())
                    .build();

            resetPasswordRepository.save(resetPassTokenEntity);

            ResetPasswordTokenCreatedEvent event = ResetPasswordTokenCreatedEvent.builder()
                    .email(user.getEmail())
                    .phone(user.getTelephone())
                    .tenantName(user.getTenant().getName())
                    .token(jwtResetToken)
                    .username(user.getUsername())
                    .tenantId(tenant.getId())
                    .build();

            applicationEventPublisher.publishEvent(event);
            rateLimitService.clearAttempts(key,RESET_REQUEST_LIMIT);
        }
        catch (TenantServiceException | UserServiceException ex) {
            rateLimitService.recordFailedAttempt(RESET_REQUEST_LIMIT, key, Duration.ofMinutes(15));
            throw new ResetPasswordServiceException("Invalid request for reset password"+ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    public void confirmResetPassword(ConfirmResetPassRequest request){

        String key = request.getUsername()  + ":"+ request.getTenantName();
        // Verificar si ya superó el límite
        if (rateLimitService.isLimitExceeded(RESET_CONFIRM_LIMIT, key, MAX_CONFIRM_RESET_ATTEMPTS)) {
            notificateConfirmResetLimitExceeded(request.getUsername(),request.getTenantName());
            throw new ResetPasswordServiceException("Too many reset requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        }
        try {

            // get claims
            UUID tenantId = jwtService.getTenantIdFromToken(request.getToken());
            String username = jwtService.getUsernameFromToken(request.getToken());

            // get user and tenant
            UserEntity user = userService.getUserByUsernameAndTenant(username, tenantId);


            // basic validations
            basicValidations(user, request);

            //jwt validation
            if (!jwtService.isTokenValid(request.getToken(), new CustomUserDetails(user, null))) {
                throw new ResetPasswordServiceException("Token invalid or expired");
            }

            // search valid tokens not used
            List<ResetPasswordToken> tokens = resetPasswordRepository
                    .findAllByUserIdAndTenantIdAndIsUsedFalse(user.getId(), tenantId).orElseThrow(
                            () -> new ResetPasswordServiceException("token not found or used")
                    );

            ResetPasswordToken validToken = tokens.stream()
                    .filter(t -> passwordEncoder.matches(request.getToken(), t.getTokenHash()))
                    .findFirst()
                    .orElseThrow(() -> new ResetPasswordServiceException(
                            "Token invalid , not match with any active token"));

            if (validToken.isRevoked()) {
                throw new ResetPasswordServiceException("Token is revoked");
            }

            validToken.setUsed(true);
            resetPasswordRepository.save(validToken);

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            authService.logoutAllSession(user.getId(), user.getTenant().getId());

            applicationEventPublisher.publishEvent(ResetPasswordConfirmEvent.builder()
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .tenantId(user.getTenant().getId())
                    .tenantName(user.getTenant().getName())
                    .build()
            );

            rateLimitService.clearAttempts(key,RESET_CONFIRM_LIMIT);
            log.info("Password reset ok to user {} of tenant id  {}", username, tenantId);
        }
        catch (TenantServiceException | UserServiceException ex) {
            rateLimitService.recordFailedAttempt(RESET_REQUEST_LIMIT, key, Duration.ofMinutes(20));
            throw new ResetPasswordServiceException("Invalid confirm request for reset password "+ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (ResetPasswordServiceException ex) {
            rateLimitService.recordFailedAttempt(RESET_REQUEST_LIMIT, key, Duration.ofMinutes(20));
            throw ex;
        }
    }

    private void notificateConfirmResetLimitExceeded(String username, String tenantName) {
        TenantEntity tenant = tenantService.getTenantByName(tenantName);
        UserEntity user = userService.getUserByUsernameAndTenant(username,tenant.getId());
        applicationEventPublisher.publishEvent(ResetPasswordConfirmLimitRequestExceededEvent.builder()
                .tenantName(tenantName)
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantId(tenant.getId())
                .build());

    }
    private void notificateRequestResetLimitExceeded(String username, String tenantName) {
        TenantEntity tenant = tenantService.getTenantByName(tenantName);
        UserEntity user = userService.getUserByUsernameAndTenant(username,tenant.getId());
        applicationEventPublisher.publishEvent(ResetPasswordRequestTokenExceededEvent.builder()
                .tenantName(tenantName)
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantId(tenant.getId())
                .attemptCount(MAX_REQUEST_RESET_ATTEMPTS)
                .build());

    }


    //aux confirmResetPassword
    private void basicValidations(UserEntity user ,ConfirmResetPassRequest request){
        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new ResetPasswordServiceException("User is not active");
        }
        if (!user.getTenant().getName().equals(request.getTenantName())) {
            throw new ResetPasswordServiceException("Token tampered: tenant mismatch");
        }
        if (!user.getUsername().equals(request.getUsername())) {
            throw new ResetPasswordServiceException("Token tampered: username mismatch");
        }

    }
    //mask with *  email
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new AuthServiceException("Email invalid");
        }

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        // Enmascarar parte local
        String maskedLocal = maskPart(local,2,1);

        // Separar dominio en nombre y TLD
        int dotIndex = domain.lastIndexOf('.');
        if (dotIndex <= 1) { // dominio muy corto
            return maskedLocal + "@" + maskPart(domain,1,0);
        }

        String domainName = domain.substring(0, dotIndex);
        String domainTld = domain.substring(dotIndex); // incluye el '.'

        String maskedDomain = maskPart(domainName,1,0) + domainTld;

        return maskedLocal + "@" + maskedDomain;
    }
    //auxiliar to maskEmail
    private String maskPart(String part ,Integer startVisible, Integer endVisible) {
        if (part.length() <= 2) {
            return part.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(part.substring(0, startVisible));
        for (int i = 0; i < part.length() - startVisible - endVisible; i++) {
            sb.append("*");
        }
        sb.append(part.substring(part.length() - endVisible));
        return sb.toString();
    }


}
