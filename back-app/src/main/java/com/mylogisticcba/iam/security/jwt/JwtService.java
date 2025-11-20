package com.mylogisticcba.iam.security.jwt;

import com.mylogisticcba.iam.security.auth.securityCustoms.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.function.Function;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private  String SECRET_KEY;

    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);

    }

    private String getToken(Map<String,Object> extraClaims, UserDetails user) {
        if (user instanceof CustomUserDetails) {
            extraClaims.put("tenantID",  Objects.toString (((CustomUserDetails) user).getTenantId().toString()));
            extraClaims.put("globalTokenVersion",Objects.toString (((CustomUserDetails) user).getGlobalTokenVersion(),null));
            extraClaims.put("sessionId", Objects.toString(((CustomUserDetails) user).getSessionId(),null));


        }
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))//15 minutes
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes=Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public UUID getGlobalVersionToken(String token) {
        String tokenStr = getClaim(token,claims-> Objects.toString( claims.get("globalTokenVersion"),null));
        return UUID.fromString(tokenStr);
    }
    public UUID getSessionIdFromToken(String token) {
        String sessionStr = getClaim(token,claims -> Objects.toString(claims.get("sessionId"),null));
        return UUID.fromString(sessionStr);
    }

    public UUID getTenantIdFromToken(String token) {
        String tenantIdStr = getClaim(token, claims -> claims.get("tenantID", String.class));
        return UUID.fromString(tenantIdStr);
    }
/*
    public boolean isTokenValid(String token, CustomUserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        final UUID tenantId   = getTenantIdFromToken(token);
        final UUID sessionId = getSessionIdFromToken(token);
         UUID globalVersion = null;
        //validacion de uuid seteado para el jwt de reseteo de contraseña (no tendra global) todos los otros casos si
        if(!sessionId.equals(UUID.fromString("1e522963-af4c-43e6-85aa-5dab290be13c"))) {
            globalVersion = getGlobalVersionToken(token);
        }

        if (!StringUtils.hasText(username) || tenantId == null) {
            return false;
        }

        return username.equals(userDetails.getUsername()) &&
                tenantId.equals(userDetails.getTenantId()) &&
                //validacion si el token de de reseteo de contraseña
                (globalVersion.equals(userDetails.getGlobalTokenVersion())||sessionId.equals(UUID.fromString("1e522963-af4c-43e6-85aa-5dab290be13c")))&&
                Objects.equals(sessionId, userDetails.getSessionId())&&
                !isTokenExpired(token);
    }
*/
    public boolean isTokenValid(String token, CustomUserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        final UUID tenantId   = getTenantIdFromToken(token);
        final UUID sessionId  = getSessionIdFromToken(token);
        UUID globalVersion    = null;

        final UUID RESET_PASSWORD_SESSION_UUID = UUID.fromString("1e522963-af4c-43e6-85aa-5dab290be13c");

        // Si no es el token de reseteo, obtengo la versión global
        if (!Objects.equals(sessionId, RESET_PASSWORD_SESSION_UUID)) {
            globalVersion = getGlobalVersionToken(token);
        }

        if (!StringUtils.hasText(username) || tenantId == null) {
            return false;
        }

        return Objects.equals(username, userDetails.getUsername())
                && Objects.equals(tenantId, userDetails.getTenantId())
                && (
                        // Si tiene globalVersion, debe coincidir
                        Objects.equals(globalVersion, userDetails.getGlobalTokenVersion())
                        // O si es el token especial de reset, también es válido
                        || Objects.equals(sessionId, RESET_PASSWORD_SESSION_UUID)
                    )
                && (
                        Objects.equals(sessionId, userDetails.getSessionId())
                        || Objects.equals(sessionId, RESET_PASSWORD_SESSION_UUID))
                && !isTokenExpired(token);
    }








    private Claims getAllClaims(String token)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims,T> claimsResolver)
    {
        final Claims claims=getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token)
    {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token)
    {
        return getExpiration(token).before(new Date());
    }

}
