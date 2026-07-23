package com.example.indbank.PolicyDecisionPointService.Utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {

    private final String SECRET = "dGhpc0lzQVN1cGVyU3Ryb25nUmFuZG9tSldUU2VjcmV0S2V5MTIzNDU2Nzg5";

    @Getter
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generate_policyCustomerJwt(
            String customerId,
            String customerServiceId,
            String channel,
            String scope,
            String authLevel,
            String issuer
    ) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 60_000);

        return Jwts.builder()
                .subject(customerId)
                .claim("customerServiceId", customerServiceId)
                .claim("channel", channel)
                .claim("scope", List.of(scope))
                .claim("mcm", authLevel)
                .claim("issuer", issuer)
                .setHeaderParam("kid", SECRET)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateOAuthCustomerJwt(
            String sub,
            String customerId,
            String customerServiceId,
            String clientId,
            String token_type,
            String channel,
            String scope,
            String authLevel,
            String issuer,
            String jti) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 60_000); // 1 minute

        return Jwts.builder()
                .setSubject(sub)
                .setIssuer(issuer)
                .setId(jti)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setHeaderParam("client_id", clientId)
                .setHeaderParam("kid", SECRET)
                .claim("customerId", customerId)
                .claim("customerServiceId", customerServiceId)
                .claim("token_type", token_type)
                .claim("channel", channel)
                .claim("scope", List.of(scope))
                .claim("mcm", authLevel)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
