package snghk.mineus.mineserver.dto;

import lombok.Builder;
import lombok.Data;
import snghk.mineus.mineserver.entity.MCServer;

@Builder
@Data
public class ServerInstanceResponse {
    private Long id;
    private String serverName;

    public static ServerInstanceResponse from(MCServer server) {
        return ServerInstanceResponse.builder()
                .id(server.getId())
                .serverName(server.getServerName())
                .build();
    }
}
