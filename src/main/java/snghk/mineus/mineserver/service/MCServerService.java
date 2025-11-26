package snghk.mineus.mineserver.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snghk.mineus.exception.ServerLimitExceededException;
import snghk.mineus.mineserver.dto.ServerCreateRequest;
import snghk.mineus.mineserver.entity.MCServer;
import snghk.mineus.mineserver.repository.MCServerRepository;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Transactional(readOnly = true)
@Service
public class MCServerService {
    @Autowired
    private DockerClient dockerClient;
    @Autowired
    private MCServerRepository mcServerRepository;

    private final int START_PORT = 25565;

    @Transactional
    public MCServer createServer(
            ServerCreateRequest request,
            Long userId
    ) {
        // 1. [검증] 사용자당 1개 서버 생성 제한
        validateOneServerPerUser(userId);

        // 2. [DB] 동적 포트 할당 (가장 큰 포트 + 1)
        int newPort = assignNewPort();

        MCServer mcServer = new MCServer();
        mcServer.setServerName(request.getServerName());
        mcServer.setPort(newPort);
        mcServer.setRunning(false);
        mcServer.setOwnerUser("test1");
        mcServer.setUserId(userId);
        mcServerRepository.save(mcServer);

        String containerId = startDockerContainer(mcServer);

        mcServer.setContainerId(containerId);
        mcServer.setRunning(true);

        return mcServer;
    }

    public List<MCServer> getServersByUserId(Long userId) {
        return mcServerRepository.findAllByUserId(userId);
    }

    // 스캐닝 방지 (자원 열거형 공격 방지)
    public MCServer getServerByIdAndUserId(Long serverId, Long userId) {
        return mcServerRepository.findByIdAndUserId(serverId, userId).orElseThrow(() -> new RuntimeException("Server not found"));
    }

    // === private methods ===

    int assignNewPort() {
        Integer maxPort = mcServerRepository.findMaxPort();
        int newPort = (maxPort==null) ? START_PORT : maxPort + 1;
        return newPort;
    }

    // 3. Docker 실행 메서드 (복잡한 로직 분리)
    private String startDockerContainer(MCServer server) {
        try {
            // 이미지 설정
            // 
            String image = "itzg/minecraft-server:java21-graalvm";

            // 볼륨 경로 설정 (Windows 사용자 홈 디렉토리 기준)
            // 예: C:/Users/{Username}/mineus-data/server-1
            String hostDataPath = System.getProperty("user.home") + "/mineus-data/server-" + server.getId();
            new File(hostDataPath).mkdirs(); // 폴더 실제 생성

            // 1. 컨테이너 내부 포트 (TCP 25565)
            ExposedPort tcp25565 = ExposedPort.tcp(25565);

            // 2. 포트 바인딩 설정 객체
            Ports portBindings = new Ports();

            portBindings.bind(tcp25565, Ports.Binding.bindPort(server.getPort()));

            Bind volumeBind = new Bind(hostDataPath, new Volume("/data"));

            // 볼륨 & 포트 바인딩
            HostConfig hostConfig = new HostConfig()
                    .withPortBindings(portBindings)
                    .withBinds(volumeBind);
            
            // 환경 변수 설정
            List<String> envVars = Arrays.asList(
                    "EULA=TRUE",
                    "VERSION=1.21.10",
                    "TYPE=PAPER",
                    "MOTD=" + server.getServerName()
            );

            // 컨테이너 생성
            CreateContainerResponse container = dockerClient.createContainerCmd(image)
                    .withName("mineus-server-" + server.getId()) // 컨테이너 이름: mineus-server-1
                    .withEnv(envVars)
                    .withHostConfig(hostConfig)
                    .withExposedPorts(ExposedPort.tcp(25565))
                    .exec();

            // 컨테이너 시작
            dockerClient.startContainerCmd(container.getId()).exec();

            return container.getId();

        } catch (Exception e) {
            // Docker 실행 실패 시, DB에 저장된 데이터도 롤백하기 위해 RuntimeException을 던짐
            throw new RuntimeException("서버 시작 실패: " + e.getMessage(), e);
        }
    }

    // 1. 중복 검사 메서드
    private void validateOneServerPerUser(Long userId) {
        // DB에 해당 유저의 서버가 이미 존재하는지 확인
        if (mcServerRepository.existsByUserId(userId)) {
            throw new ServerLimitExceededException("이미 생성된 서버가 있습니다. (사용자당 1개 제한)");
        }
    }
}
