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
            extraClaims.put("tenantID", ((CustomUserDetails) user).getTenantId().toString());
            extraClaims.put("globalTokenVersion",((CustomUserDetails) user).getGlobalTokenVersion().toString());
            extraClaims.put("sessionId", ((CustomUserDetails) user).getSessionId().toString());
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
        String tokenStr = getClaim(token,claims-> claims.get("globalTokenVersion").toString());
        return UUID.fromString(tokenStr);
    }
    public UUID getSessionIdFromToken(String token) {
        String sessionStr = getClaim(token,claims -> claims.get("sessionId").toString());
        return UUID.fromString(sessionStr);
    }

    public UUID getTenantIdFromToken(String token) {
        String tenantIdStr = getClaim(token, claims -> claims.get("tenantID", String.class));
        return UUID.fromString(tenantIdStr);
    }

    public boolean isTokenValid(String token, CustomUserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        final UUID tenantId   = getTenantIdFromToken(token);
        final UUID globalVersion = getGlobalVersionToken(username);
        final UUID sessionId = getSessionIdFromToken(token);
        if (!StringUtils.hasText(username) || tenantId == null) {
            return false;
        }

        return username.equals(userDetails.getUsername()) &&
                tenantId.equals(userDetails.getTenantId()) &&
                globalVersion.equals(userDetails.getGlobalTokenVersion())&&
                Objects.equals(sessionId, userDetails.getSessionId())&&
                !isTokenExpired(token);
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
