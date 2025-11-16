package com.mylogisticcba.iam.security.auth.securityCustoms;
import com.mylogisticcba.iam.repositories.UserRepository;
import java.util.UUID;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("tenantProvider")
@RequiredArgsConstructor
public class TenantAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TenantAwareAuthenticationToken token = (TenantAwareAuthenticationToken) authentication;
        String username = token.getName();
        String password = token.getCredentials().toString();
        UUID tenantId = token.getTenantId();
        UUID sessionId = token.getSessionId();

        // Buscar usuario por username + tenant
        UserEntity user = userRepository.findByUsernameAndTenant_Id(username, tenantId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // Validar password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(user,null);
        // Devolver token autenticado con authorities
        return new TenantAwareAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities(),
                tenantId,
                null
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TenantAwareAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
