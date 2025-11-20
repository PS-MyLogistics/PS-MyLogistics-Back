package com.mylogisticcba.iam.security.jwt;

import com.mylogisticcba.iam.repositories.RefreshTokenRepository;
import com.mylogisticcba.iam.repositories.TenantRepository;
import com.mylogisticcba.iam.repositories.UserRepository;
import com.mylogisticcba.iam.security.auth.entity.RefreshToken;
import com.mylogisticcba.iam.security.auth.exceptions.AuthServiceException;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.iam.security.auth.securityCustoms.CustomUserDetails;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter  extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TenantRepository tenantRepository; // 🔹 para validar existencia
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        System.out.println("URI: " + request.getRequestURI());
        String path = request.getRequestURI();
        return path.startsWith("/auth/");
    }


    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("request llego al doFilterInternal" );

        TenantContextHolder.clear();

        final String token = getTokenFromRequest(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        // Check if the user is already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
                String username = jwtService.getUsernameFromToken(token);
                UUID tenantId = jwtService.getTenantIdFromToken(token);
                UUID sessionId =jwtService.getSessionIdFromToken(token);

                // basic validation token
                if (username == null || tenantId == null||sessionId == null) {
                    handleAuthenticationError(response, "Authentication failed");
                    return;
                }

                // validate tenant existence
                TenantEntity tenantEntity = tenantRepository.findById(tenantId)
                        .orElseThrow(() -> new AuthServiceException("Authentication failed"));

                UserEntity userEntity = userRepository.findByUsernameAndTenant_Id(username,tenantId).orElseThrow(()
                        -> new AuthServiceException("Authentication failed"));

                RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevoked(sessionId,false)
                        .orElseThrow(() -> new AuthServiceException("Authentication failed"));


                //validate pertenence
                if (!tenantEntity.getId().equals(userEntity.getTenant().getId())) {
                    handleAuthenticationError(response, "Authentication failed");
                    return;
                }


                // get userdetails of the entity
                CustomUserDetails customUserDetails = new CustomUserDetails(userEntity,refreshToken.getToken());

                //  validate token
                if (!jwtService.isTokenValid(token, customUserDetails)) {
                    handleAuthenticationError(response, "Authentication failed");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                TenantContextHolder.setTenant(tenantId);

                // Debug: imprimir información de autenticación para diagnosticar 403 en endpoints específicos
                logger.info("[JWT-AUTH] Authentication set. Principal=" + SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getSimpleName());
                logger.info("[JWT-AUTH] Authorities=" + SecurityContextHolder.getContext().getAuthentication().getAuthorities());

                filterChain.doFilter(request, response);
        }
          catch (AuthServiceException e) {
            // bussiness logic errors (tenant not found, user not found, etc.)
            handleAuthenticationError(response,"Authentication AuthException failed" );
        } catch (Exception e) {
            // jwt errors (invalid token, expired token, etc.)
            handleAuthenticationError(response, "Authentication Exception failed");
        }
        finally {
            // Clear the tenant context after processing the request
            TenantContextHolder.clear();
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")){
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private void handleAuthenticationError(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
