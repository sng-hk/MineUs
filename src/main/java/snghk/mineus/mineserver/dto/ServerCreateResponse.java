package snghk.mineus.mineserver.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import snghk.mineus.mineserver.entity.MCServer;

@Getter
@Builder
public class ServerCreateResponse {
    private String serverName;
    private int port;
    private String containerId;
    private boolean isRunning;
    private Long userId;

    public static ServerCreateResponse from(MCServer server) {
        return ServerCreateResponse.builder()
                .serverName(server.getServerName())
                .port(server.getPort())
                .containerId(server.getContainerId())
                .isRunning(server.isRunning())
                .userId(server.getUserId())
                .build();
    }

}
