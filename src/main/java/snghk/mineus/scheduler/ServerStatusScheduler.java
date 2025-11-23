package snghk.mineus.scheduler;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import snghk.mineus.mineserver.entity.MCServer;
import snghk.mineus.mineserver.repository.MCServerRepository;
import snghk.mineus.mineserver.service.MCServerService;

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

    private void updateServerStatus(MCServer server) {
        try {
            // 2. Docker에게 상태 물어보기 (docker inspect)
            InspectContainerResponse info = dockerClient.inspectContainerCmd(server.getContainerId()).exec();

            // 3. 상태 판단 로직
            String dockerState = info.getState().getStatus(); // "running", "exited" ...
            // Docker HealthCheck 결과 ("healthy", "starting", "unhealthy")
            String healthStatus = info.getState().getHealth().getStatus();

            if(!dockerState.equals("running") || !healthStatus.equals("healthy")) {
                server.setRunning(false);
                mcServerRepository.save(server);
                }
//            if ("running".equals(dockerState)) { // Container status
//                if ("healthy".equals(healthStatus)) { // minecraft server status
//                    server.setRunning(false);
//                }
//            }
//
//            // 4. DB와 다르면 업데이트 (변경 감지)
//            if (!server.isRunning())) {
//                log.info("✅ 상태 변경 감지: {} ({} -> {})", server.getServerName(), server.isRunning(), "false");
//                server.isRunning(newStatus);
//                mcServerRepository.save(server);
//            }

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
