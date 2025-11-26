package snghk.mineus.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import snghk.mineus.auth.dto.LoginRequest;
import snghk.mineus.auth.dto.RegisterRequest;
import snghk.mineus.auth.jwt.JwtTokenProvider;
import snghk.mineus.user.Role;
import snghk.mineus.user.User;
import snghk.mineus.user.UserRepository;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에 등록한 것
    private final JwtTokenProvider tokenProvider;  // 방금 만든 공장

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            userRepository.save(
                    User.builder()
                            .email(request.getEmail())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .nickname(request.getNickname())
                            .role(Role.USER)
                            .build()
            );
        }
        return ResponseEntity.status(200).body("회원가입 완료");
    }

    // 로그인 (JWT token 발급)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = null;
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            token = tokenProvider.createToken(user);
        } else {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return ResponseEntity.status(200).body(token);
    }
}
