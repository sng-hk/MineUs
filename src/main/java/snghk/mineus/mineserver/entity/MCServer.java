package snghk.mineus.mineserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
public class MCServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serverName;
    private String ownerUser;

    @Column
    private int port;

    @Column(unique = true) // 컨테이너 ID도 중복되지 않도록 지정
    private String containerId;

    private boolean isRunning;

    private Long userId;
}
