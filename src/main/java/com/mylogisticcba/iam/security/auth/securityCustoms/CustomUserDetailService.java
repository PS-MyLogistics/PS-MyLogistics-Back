package com.mylogisticcba.iam.security.auth.securityCustoms;

import com.mylogisticcba.iam.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID tenantId = TenantContextHolder.getTenant();
        log.info("Cargando usuario {} para tenant {}", username, tenantId);

        var entity = userRepository.findByUsernameAndTenant_Id(username, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
         return new CustomUserDetails(entity,null);
    }
}