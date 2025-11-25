package snghk.mineus.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import snghk.mineus.user.User;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long tokenValidityInMilliseconds;

    // 1. 생성자: application.properties에서 비밀키와 유효시간을 가져와서 세팅
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long tokenValidityInMilliseconds) {
        // 비밀키를 암호화 알고리즘에 쓸 수 있는 형태로 변환
//        byte[] keyBytes = Decoders.BASE64.decode(secret); // Base64 인코딩된 key를 사용할 경우, 디코딩

        // 만약 Base64 인코딩 안 된 문자열이라면 비밀키로 바로 사용
        this.key = Keys.hmacShaKeyFor(secret.getBytes());

        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
    }

    // 2. 토큰 생성 (발급)
    public String createToken(User user) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(user.getId())) // 토큰 제목(Subject)에 userId 저장
                .claim("email", user.getEmail())           // 추가 정보로 email 저장
                .signWith(key)                   // 서명
                .expiration(validity)            // 유효기간
                .compact();
    }

    // 3. 토큰에서 userId 꺼내기 (해석)
    public Long getUserId(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(key) // 비밀키로 서명 확인
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject() // 아까 넣은 userId 꺼냄
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject(); // userId

            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
