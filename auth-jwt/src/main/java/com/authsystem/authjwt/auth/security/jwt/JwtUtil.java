package com.authsystem.authjwt.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private String issuer;
    private SecretKey secretKey;

    public JwtUtil(@Value("${jwt.issuer}")String issuer,
                   @Value("${jwt.secret}")String secret) {
        this.issuer = issuer;
        // String 타입의 secret 을 객체변수(secretKey) 로 암호화
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // 클레임에서 JTI(JWT ID(를 추출하는 메서드
    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    public String getCategory(String token){
        return getClaims(token).get("category", String.class);
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 토큰이 만료되기까지 남은 시간을 밀리초(ms)로 반환하며, 이미 만료된 경우 0을 반환한다.
    public long getRemainingExpirationMillis(String token) {
        Date expiration = getClaims(token).getExpiration();
        return Math.max(expiration.getTime() - System.currentTimeMillis(), 0);
    }

    // 토큰이 현재 시점 기준으로 만료되었는지 여부를 반환한다.
    public boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Jwt 생성
    public String createJwt(String category, String username, String role,Long expiredMs){
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .id(UUID.randomUUID().toString())
                .issuer(this.issuer)
                .claim("category",category)
                .claim("username",username)
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // JWT에서 클레임을 추출하는 메서드
    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 만료되었더라도 payload 자체는 필요할 수 있으므로 반환
            return e.getClaims();
        }
    }
}
