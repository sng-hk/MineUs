package snghk.mineus.mineserver.dto;

import lombok.Builder;
import snghk.mineus.mineserver.entity.MCServer;

@Builder
public class ServerDetailResponse {
    private Long id;
    private String serverName;
    private int port;
    private String ipAddress;
    private boolean isRunning;
    private String version;

    // private String ramUsage;
    // private String cpuUsage;
    // private List<String> logs;

    public static ServerDetailResponse from(MCServer server) {
        return ServerDetailResponse.builder()
                .id(server.getId())
                .serverName(server.getServerName())
                .port(server.getPort())
                .ipAddress("127.0.0.1")
                .isRunning(server.isRunning())
                .version("1.21.10")
                .build();
    }
}