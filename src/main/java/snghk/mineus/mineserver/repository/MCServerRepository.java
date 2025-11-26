package snghk.mineus.mineserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import snghk.mineus.mineserver.entity.MCServer;

import java.util.List;
import java.util.Optional;

@Repository
public interface MCServerRepository extends JpaRepository<MCServer, Long> {
    List<MCServer> findAllByUserId(Long userId);
    Optional<MCServer> findByIdAndUserId(Long serverId, Long userId);
    Boolean existsByUserId(Long userId);

    @Query("SELECT MAX(s.port) FROM MCServer s")
    Integer findMaxPort();

    @Query("SELECT s FROM MCServer s WHERE s.isRunning = true")
    List<MCServer> findRunningServers();
}