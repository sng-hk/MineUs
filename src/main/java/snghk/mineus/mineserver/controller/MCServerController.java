package snghk.mineus.mineserver.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*; // (import를 java.util.List 대신 이걸로)
import com.github.dockerjava.core.command.PullImageResultCallback;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snghk.mineus.mineserver.dto.ServerCreateRequest;
import snghk.mineus.mineserver.dto.ServerCreateResponse;
import snghk.mineus.mineserver.entity.MCServer;
import snghk.mineus.mineserver.service.MCServerService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MCServerController {

    private final MCServerService mcServerService;

    @PostMapping("/api/start")
    public ResponseEntity<ServerCreateResponse> startServer(
            @RequestBody  @Valid ServerCreateRequest request
    ) throws InterruptedException {
        MCServer server = mcServerService.createServer(request);
        ServerCreateResponse response = ServerCreateResponse.from(server);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ... /api/stop 메서드는 동일 ...
//    @GetMapping("/api/stop")
//    public String stopServer() {
//        if (this.containerId == null) {
//            return "실행 중인 서버가 없습니다.";
//        }
//        try {
//            System.out.println(this.containerId + " 컨테이너를 중지합니다...");
//            dockerClient.stopContainerCmd(this.containerId).exec();
//            System.out.println(this.containerId + " 컨테이너를 삭제합니다...");
//            dockerClient.removeContainerCmd(this.containerId).exec();
//            String stoppedId = this.containerId;
//            this.containerId = null;
//            return "서버 중지 및 삭제 완료. ID: " + stoppedId;
//        } catch (Exception e) {
//            this.containerId = null;
//            return "오류 발생 (컨테이너가 이미 중지되었을 수 있음): " + e.getMessage();
//        }
//    }
}