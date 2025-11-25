package snghk.mineus.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.bind.annotation.RequestBody;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // 로그인 ID

    @Column(nullable = false)
    private String password; // ★ 암호화된 비밀번호 (BCrypt)

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role; // ROLE_USER, ROLE_ADMIN

    @Builder
    public User(String email, String password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }
}
