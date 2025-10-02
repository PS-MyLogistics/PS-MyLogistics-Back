package com.mylogisticcba.iam.security.auth.securityCustoms;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class TenantAwareAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final UUID tenantId;
    private final UUID sessionId;

    // Para login inicial (sin authorities)
    public TenantAwareAuthenticationToken(String username, String password, UUID tenantId, UUID sessionId) {
        super(username, password);
        this.tenantId = tenantId;
        this.sessionId = sessionId;
    }

    // Para usuario ya autenticado (con authorities)
    public TenantAwareAuthenticationToken(Object principal, Object credentials,
                                          Collection<? extends GrantedAuthority> authorities,
                                          UUID tenantId, UUID sessionId) {
        super(principal, credentials, authorities);
        this.tenantId = tenantId;
        this.sessionId = sessionId;
    }

    public UUID getSessionId() {return sessionId;}
    public UUID getTenantId() {
        return tenantId;
    }
}
