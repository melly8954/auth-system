package com.authsystem.authjwt.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    public String getCategory(String token){
        return getClaims(token).get("category", String.class);
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String getTokenId(String token) {
        return getClaims(token).get("tokenId", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            // parseSignedClaims 시 이미 만료 검사 포함
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return false; // 예외 없으면 만료되지 않음
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;  // 만료됨
        }
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

    public long getExpiration(String token) {
        try {
            // 토큰 검증 + 파싱
            Jwt<?, Claims> jwt = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jwt.getPayload();
            Date exp = claims.getExpiration(); // "exp"를 Date로 가져옴
            return exp.getTime() - System.currentTimeMillis();

        } catch (ExpiredJwtException e) {
            // 이미 만료된 경우, 남은 시간은 0
            return 0;
        }
    }

    // JWT에서 클레임을 추출하는 메서드
    private Claims getClaims(String accessToken) {
        // 토큰이 만료되면 parseSignedClaims() 메서드에서
        // ExpiredJwtException 예외가 발생하여 코드가 실행되지 않기 때문에
        // ExpiredJwtException 예외가 발생해도 클레임을 반환하도록 예외 처리를 한다.
        try {
            return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
