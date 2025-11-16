package snghk.itsmine;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*; // (import를 java.util.List 대신 이걸로)
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
public class MCServerController {

    @Autowired
    private DockerClient dockerClient;
    private String containerId = null;

    @GetMapping("/api/start")
    public String startServer() throws InterruptedException {

        if (this.containerId != null) {
            return "이미 실행 중인 서버가 있습니다: " + this.containerId;
        }

        // 1. 이미지 (태그 확인: java17 사용)
        String image = "itzg/minecraft-server:java21-graalvm";
        System.out.println(image + " 이미지를 PULL 합니다...");
        // (pull은 PoC 1단계에서 수동으로 했다고 가정하고, 이 라인을 주석 처리하거나 놔둬도 됩니다)
         dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion();

        // 2. ★★★ 환경 변수 (List<String>으로 명시적 전달) ★★★
        List<String> envVars = Arrays.asList(
                "EULA=TRUE",
                "VERSION=1.21.10", // 1.21은 java17과 호환됩니다.
                "TYPE=PAPER"
        );

        // 3. ★★★ 포트 바인딩 (가장 표준적인 방식) ★★★
        ExposedPort tcp25565 = ExposedPort.tcp(25565);
        Ports portBindings = new Ports();
        portBindings.bind(tcp25565, Ports.Binding.bindPort(25565)); // 25565:25565

        // 4. ★★★ 볼륨 바인딩 (공식 문서의 '-v mc-data:/data' 방식) ★★★
        // "mc-data-poc" 라는 '이름을 가진 볼륨'을 컨테이너의 /data에 연결합니다.
        // 이 볼륨은 Docker가 알아서 관리해 줍니다. (PoC 2단계의 핵심)
        Volume volume = new Volume("/data");
        Bind bind = new Bind("mc-data-poc-" + UUID.randomUUID().toString().substring(0, 8), volume);

        // 5. ★★★ HostConfig (포트 + 볼륨) ★★★
        HostConfig hostConfig = new HostConfig()
                .withPortBindings(portBindings)
                .withBinds(bind);

        // 6. 컨테이너 생성 (모든 설정을 명시적으로 전달)
        String containerName = "itsmine-poc-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("컨테이너를 생성합니다. 이름: " + containerName);

        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withName(containerName)
                .withEnv(envVars)             // ★ List로 전달
                .withExposedPorts(tcp25565)
                .withHostConfig(hostConfig)   // ★ HostConfig 전달
                .exec();

        this.containerId = container.getId();

        // 7. 컨테이너 시작
        dockerClient.startContainerCmd(this.containerId).exec();
        System.out.println(this.containerId + " 컨테이너를 시작합니다...");

        return "서버 시작 요청 완료! Container ID: " + this.containerId;
    }

    // ... /api/stop 메서드는 동일 ...
    @GetMapping("/api/stop")
    public String stopServer() {
        if (this.containerId == null) {
            return "실행 중인 서버가 없습니다.";
        }
        try {
            System.out.println(this.containerId + " 컨테이너를 중지합니다...");
            dockerClient.stopContainerCmd(this.containerId).exec();
            System.out.println(this.containerId + " 컨테이너를 삭제합니다...");
            dockerClient.removeContainerCmd(this.containerId).exec();
            String stoppedId = this.containerId;
            this.containerId = null;
            return "서버 중지 및 삭제 완료. ID: " + stoppedId;
        } catch (Exception e) {
            this.containerId = null;
            return "오류 발생 (컨테이너가 이미 중지되었을 수 있음): " + e.getMessage();
        }
    }
}