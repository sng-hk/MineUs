package snghk.mineus.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 회원가입
    Optional<User> findByEmail(String email);

    // email 중복 검사
    boolean existsByEmail(String email);
}
