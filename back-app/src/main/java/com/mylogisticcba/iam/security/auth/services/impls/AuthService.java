package com.mylogisticcba.iam.security.auth.services.impls;

import com.fasterxml.jackson.databind.JsonSerializable;
import com.mylogisticcba.common.rateLimit.RateLimitInfo;
import com.mylogisticcba.common.rateLimit.RateLimitService;
import com.mylogisticcba.common.rateLimit.RateLimitType;
import com.mylogisticcba.iam.events.DailyFailLoginExceededEvent;
import com.mylogisticcba.iam.events.FailLoginExceededEvent;
import com.mylogisticcba.iam.events.TenantCreatedEvent;
import com.mylogisticcba.iam.repositories.TenantRepository;
import com.mylogisticcba.iam.repositories.UserRepository;
import com.mylogisticcba.iam.repositories.VerificarionTokenRepository;
import com.mylogisticcba.iam.security.auth.dtos.req.LoginRequest;
import com.mylogisticcba.iam.security.auth.dtos.req.RegisterOwnerRequest;
import com.mylogisticcba.iam.security.auth.dtos.response.AuthResponse;
import com.mylogisticcba.iam.security.auth.dtos.response.LoginResponse;
import com.mylogisticcba.iam.security.auth.entity.RefreshToken;
import com.mylogisticcba.iam.security.auth.entity.VerificationToken;
import com.mylogisticcba.iam.security.auth.exceptions.AuthServiceException;
import com.mylogisticcba.iam.security.auth.securityCustoms.CustomUserDetails;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantAwareAuthenticationToken;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.iam.security.jwt.JwtService;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.iam.tenant.enums.TenantStatus;
import com.mylogisticcba.iam.tenant.enums.UserStatus;
import com.mylogisticcba.iam.tenant.services.impl.TenantService;
import com.mylogisticcba.iam.tenant.services.impl.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements com.mylogisticcba.iam.security.auth.services.AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TenantService tenantService;
    private final UserService userService;
    private final TenantRepository tenantRepository ;
    private final PasswordEncoder passwordEncoder;
    private final VerificarionTokenRepository verificarionTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitService rateLimitService;
    private final ApplicationEventPublisher applicationEventPublisher;

    //TODO: las sig variables
    // se puede llevar a configurable por tenant con tenantConfigService o a por config global con app.properties
    private final Integer MAX_LOGIN_ATTEMPTS = 5;
    private Integer MAX_DAILY_LOGIN_ATTEMPTS = 2;

    private final Duration LOGIN_LOCKOUT = Duration.ofMinutes(15);
    private static final RateLimitType LOGIN_ATTEMPTS = RateLimitType.of("login_attempts");
    private static final RateLimitType DAYLY_LOGIN_ATTEMPTS = RateLimitType.of("login_exceeded_daily");

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {

        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null)
            throw new AuthServiceException("Username and password are required");

        if (loginRequest.getTenantName() == null)
            throw new AuthServiceException("Tenant name is required");

        // Construir key correctamente: tenant:username
        String rateLimitKey = loginRequest.getTenantName() + ":" + loginRequest.getUsername();

        // Verificar rate limit
        if (rateLimitService.isLimitExceeded(LOGIN_ATTEMPTS, rateLimitKey, MAX_LOGIN_ATTEMPTS)) {

            //verify exist
            if (!rateLimitService.isCurrentlyLocked(rateLimitKey, LOGIN_ATTEMPTS)) {

                RateLimitInfo info = rateLimitService.getRateLimitInfo(rateLimitKey,DAYLY_LOGIN_ATTEMPTS);
                int blockages = info.getCurrentAttempts();
                int dailyFailLogin = MAX_DAILY_LOGIN_ATTEMPTS ;
                if(blockages > 1) {
                    dailyFailLogin = MAX_LOGIN_ATTEMPTS * blockages;
                }

                failLoginExceeded(loginRequest,dailyFailLogin);

                if(rateLimitService.isLimitExceeded(DAYLY_LOGIN_ATTEMPTS, rateLimitKey, MAX_LOGIN_ATTEMPTS)) {

                    Integer attemptCounts = MAX_DAILY_LOGIN_ATTEMPTS*MAX_LOGIN_ATTEMPTS;
                    maxDailyFailLogin(loginRequest,attemptCounts,MAX_DAILY_LOGIN_ATTEMPTS);
                    throw new AuthServiceException("MAX daily attempts "+info.getKey(), HttpStatus.TOO_MANY_REQUESTS);

                }
                rateLimitService.recordFailedAttempt(DAYLY_LOGIN_ATTEMPTS,rateLimitKey,Duration.ofHours(24));

            }


            RateLimitInfo info = rateLimitService.getRateLimitInfo(rateLimitKey,LOGIN_ATTEMPTS);
            String message =String.format("Too many failed attempts. Try again in %d minutes.",
                    info.getTimeRemainingSeconds() / 60);

            throw new AuthServiceException(message, HttpStatus.TOO_MANY_REQUESTS);

        }

        try {
            // get tenant
            TenantEntity tenant = tenantRepository.findByName(loginRequest.getTenantName())
                    .orElseThrow(() -> new AuthServiceException("Tenant not found"));

            // generate explicit token with tenant
            TenantAwareAuthenticationToken authToken =
                    new TenantAwareAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword(),
                            tenant.getId(),
                            null
                    );

            // auth
            Authentication authentication = authenticationManager.authenticate(authToken);

            // get authenticated user
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            UserEntity user = customUserDetails.getUser();
            // valid status
            switch (tenant.getStatus()) {
                case SUSPENDED -> throw new AuthServiceException("Suspended tenant");
                case PENDING_VERIFICATION -> throw new AuthServiceException("Tenant pending verification");
                case DELETED -> throw new AuthServiceException("Tenant deleted");

            }
            switch (customUserDetails.getUser().getStatus()) {
                case SUSPENDED -> throw new AuthServiceException("Suspended user");
                case PENDING_VERIFICATION -> throw new AuthServiceException("User pending verification");
                case DELETED -> throw new AuthServiceException("User deleted");
                case FREEZED ->  throw new AuthServiceException("User freezed");
            }
            // Veriffy active sessions and set new global version if not existing
            boolean hasActiveSessions = refreshTokenService.hasActiveSessionsForUser(user.getId(), tenant.getId());
            if (!hasActiveSessions && user.getGlobalTokenVersion() == null) {
                user.setGlobalTokenVersion(UUID.randomUUID());
                userRepository.save(user);
            }

            // generate refreshToken to session
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    customUserDetails.getUser().getId(), tenant.getId());

            CustomUserDetails userDetailsWithSession = new CustomUserDetails(user, refreshToken.getToken());

            // generate jwt
            String jwt = jwtService.getToken(userDetailsWithSession);



            TenantContextHolder.setTenant(tenant.getId());

            //  login is ok clear rate limit attempts
            rateLimitService.clearAttempts(rateLimitKey,LOGIN_ATTEMPTS);

            return LoginResponse.builder()
                    .success(true)
                    .accesToken(jwt)
                    .refreshToken(refreshToken)
                    .message("Login successful")
                    .build();

        } catch (AuthenticationException | AuthServiceException e) {
            rateLimitService.recordFailedAttempt(LOGIN_ATTEMPTS, rateLimitKey, LOGIN_LOCKOUT);
            throw e;
        }
    }



    @Override
    @Transactional
    public AuthResponse registerTenantAndOwner(RegisterOwnerRequest req)  {

        // Check if the user already exists
        if (userRepository.existsByUsernameAndOwnerTrue(req.getUsername())) {
            throw new AuthServiceException("Owner Username already exists");
        }
        if (tenantRepository.existsByName(req.getTenantName())) {
            throw new AuthServiceException("Tenant name already exists");
        }
        //save entities
        TenantEntity tenant  = tenantService.createTenant(req);
        UserEntity userSaved = userService.createUserOwner(req, tenant);
        // Set the tenant in the user and  update the user
        tenant.setOwnerId(userSaved.getId());
        tenantService.updateTenant(tenant);


        VerificationToken vToken =  VerificationToken.builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID())
                .userId(userSaved.getId())
                .tenantId(tenant.getId())
                .build();

        verificarionTokenRepository.save(vToken);

        TenantCreatedEvent tCreatedEvent = TenantCreatedEvent.builder()
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .ownerUsername(userSaved.getUsername())
                .ownerEmail(userSaved.getEmail())
                .ownerPhone(userSaved.getTelephone())
                .tokenVerification(vToken.getToken())
                .tenantEmail(tenant.getContactEmail())
                .tenantPhone(tenant.getContactPhone())
                .build();

        applicationEventPublisher.publishEvent(tCreatedEvent);

        return  AuthResponse.builder()
                .token("")
                .success(true)
                .message("Tenant and Owner registered successfully. Please verify your email to activate the account.")
                .build();

    }

    @Override
    public AuthResponse verifyRegistrationInTenant(String token, String idTenant) {
        VerificationToken vToken = getValidVerificationToken(token, idTenant);

        UserEntity user = userService.getUserByIdAndTenantId(vToken.getUserId(), vToken.getTenantId());
        TenantEntity tenant = tenantService.getTenantById(vToken.getTenantId());

        previousVerificationEntities2(user,tenant);

        // Activate user
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);


        return AuthResponse.builder()
                .success(true)
                .message("Registro verificado correctamente")
                .token("")
                .build();
    }

    @Transactional
    @Override
    public AuthResponse verifyRegistrationTenantAndOwner(String token, String idTenant) {

            VerificationToken vToken = getValidVerificationToken(token, idTenant);

            UserEntity user = userService.getUserByIdAndTenantId(vToken.getUserId(), vToken.getTenantId());
            TenantEntity tenant = tenantService.getTenantById(vToken.getTenantId());

            // Verify status
            previousVerificationEntities(user, tenant);

            // update status  user and tenant
            activateTenantAndUser(user, tenant);

            return AuthResponse.builder()
                    .success(true)
                    .message("Registro verificado correctamente")
                    .token("")
                    .build();

    }

    @Transactional
    @Override
    public LoginResponse rotateRefreshToken(String cookieToken) {

        RefreshToken refToken = refreshTokenService.findByToken(UUID.fromString(cookieToken));

        refreshTokenService.verifyExpiration(refToken);

        RefreshToken rotatedToken = refreshTokenService.rotateToken(refToken);
        UserEntity user = userService.getUserByIdAndTenantId(rotatedToken.getUserId(), rotatedToken.getTenantId());
        CustomUserDetails customUserDetails = new CustomUserDetails(user,rotatedToken.getToken());

        return  LoginResponse.builder().refreshToken(rotatedToken)
                .accesToken(jwtService.getToken(customUserDetails))
                .success(true)
                .refreshToken(rotatedToken)
                .message("the rotation was successful , new access and refresh token")
                .build();
    }

    @Override
    public AuthResponse verifyAfterFreeze(String token, String tenantName, String username) {

        TenantEntity tenant = tenantService.getTenantByName(tenantName);
        UserEntity user = userService.getUserByUsernameAndTenant(username, tenant.getId());
        VerificationToken vToken = getValidVerificationToken(token, username);

        if(!tenant.getOwnerId().equals(user.getId())) {
            throw new AuthServiceException("Tenant owner and username do not match");
        }
        if(!tenant.getStatus().equals(TenantStatus.ACTIVE)) {
            throw new AuthServiceException("Tenant is not active");
        }
        if(!user.getStatus().equals(UserStatus.FREEZED)){
            throw new AuthServiceException("user is not freezed");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        vToken.setDeleted(true);
        vToken.setDeletedAt(Instant.now());
        verificarionTokenRepository.save(vToken);

        return AuthResponse.builder().success(true).token("").build();


    }

    @Transactional
    public AuthResponse logoutSession(String refreshToken) {

        RefreshToken refreshTokenEntity = refreshTokenService.findByToken(UUID.fromString(refreshToken));
        refreshTokenService.revokeToken(refreshTokenEntity);

        return AuthResponse.builder().success(true).message("Logout successful").build();

    }

    @Transactional
    public AuthResponse logoutAllSession(UUID userId, UUID idTenant) {

        refreshTokenService.deleteAllByUserIdAndTenantId(userId, idTenant);
        UserEntity user = userService.getUserByIdAndTenantId(userId, idTenant);
        // new global version (all jwt already issued stop working.)
        user.setGlobalTokenVersion(UUID.randomUUID());
        userRepository.save(user);
        return AuthResponse.builder().success(true).message("Logout successful").build();
    }

    private void failLoginExceeded(LoginRequest loginRequest, Integer attemptCounts){
        UUID tenantUUID = tenantService.getTenantByName(loginRequest.getTenantName()).getId();
        UserEntity user= userService.getUserByUsernameAndTenant(loginRequest.getUsername(), tenantUUID);

        var event= FailLoginExceededEvent.builder()
                .attemptCount(attemptCounts)
                .lastAttemptTime(Instant.now())
                .email(user.getEmail())
                .tenantId(tenantUUID)
                .tenantName(loginRequest.getTenantName())
                .username(user.getUsername()).build();

        applicationEventPublisher.publishEvent(event);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void maxDailyFailLogin(LoginRequest loginRequest, Integer attemptCounts, Integer lockouts) {

        UUID tenantUUID = tenantService.getTenantByName(loginRequest.getTenantName()).getId();
        UserEntity user= userService.getUserByUsernameAndTenant(loginRequest.getUsername(), tenantUUID);

        user.setStatus(UserStatus.FREEZED);
        userRepository.save(user);

            VerificationToken vToken =  VerificationToken.builder()
                    .id(UUID.randomUUID())
                    .token(UUID.randomUUID())
                    .userId(user.getId())
                    .tenantId(tenantUUID)
                    .expiryDate(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .build();

            verificarionTokenRepository.save(vToken);

            var event =  DailyFailLoginExceededEvent.builder()
                        .token(vToken.getToken())
                        .lockouts(lockouts)
                        .attemptCount(attemptCounts)
                        .email(user.getEmail())
                        .tenantId(tenantUUID)
                        .lastAttemptTime(Instant.now())
                        .username(user.getUsername())
                        .tenantName(user.getTenant().getName())
                        .build();
        logoutAllSession(user.getId(), tenantUUID);
        applicationEventPublisher.publishEvent(event);


    }

    private VerificationToken getValidVerificationToken(String token, String idTenant) {
        VerificationToken vToken =
                verificarionTokenRepository.findByTokenAndTenantId(UUID.fromString(token), (UUID.fromString(idTenant)))
                .orElseThrow(() -> new AuthServiceException("verify token expired or invalid"));
        if (vToken.getExpiryDate().isBefore(Instant.now())) {
            throw new AuthServiceException("verify token expired retry again your registration");
        }
        return vToken;
    }
    private void previousVerificationEntities(UserEntity user,TenantEntity tenant) {
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {

            throw new AuthServiceException("User was verificated previously his status is " + user.getStatus());

        }
        if (tenant.getStatus() != TenantStatus.PENDING_VERIFICATION) {
            throw new AuthServiceException("Tenant was verificated previously his status is " + tenant.getStatus());

        }

    }
    private void previousVerificationEntities2(UserEntity user, TenantEntity tenant) {
        if( tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new AuthServiceException("Tenant is not active his status is " + tenant.getStatus());
        }
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new AuthServiceException("User was verificated previously his status is " + user.getStatus());
        }
    }
    private void activateTenantAndUser(UserEntity user, TenantEntity tenant) {
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);
    }
}
