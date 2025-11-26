package snghk.mineus.global.scheduler;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import snghk.mineus.mineserver.entity.MCServer;
import snghk.mineus.mineserver.repository.MCServerRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerStatusScheduler {
    private final MCServerRepository mcServerRepository;
    private final DockerClient dockerClient;

    // 5000ms = 5초마다 실행
    @Scheduled(fixedRate = 5000)
    public void syncServerStatus() {
        // 1. 관리 대상 서버 조회 (STOPPED는 검사 안 함)
        List<MCServer> activeServers = mcServerRepository.findRunningServers();

        if (activeServers.isEmpty()) return; // 검사할 게 없으면 바로 종료

        log.info("🔄 [스케줄러] {}개의 활성 서버 상태를 점검합니다...", activeServers.size());

        for (MCServer server : activeServers) {
            updateServerStatus(server);
        }
    }

    @Transactional
    protected void updateServerStatus(MCServer server) {
        try {
            // docker inspect
            InspectContainerResponse info = dockerClient.inspectContainerCmd(server.getContainerId()).exec();

            String dockerState = info.getState().getStatus(); // "running", "exited" ...
            // Docker HealthCheck ("healthy", "starting", "unhealthy")
            String healthStatus = info.getState().getHealth().getStatus();

            if(!dockerState.equals("running") || !healthStatus.equals("healthy")) {
                server.setRunning(false);
            }

        } catch (NotFoundException e) {
            // Docker에 컨테이너가 없다면? (누가 수동으로 지웠거나 오류로 삭제됨)
            log.warn("⚠️ 컨테이너를 찾을 수 없음. STOPPED 처리: {}", server.getServerName());
            server.setRunning(false);
            mcServerRepository.save(server);
        } catch (Exception e) {
            log.error("❌ 상태 점검 중 오류 발생: {}", server.getServerName(), e);
        }
    }
}
