package snghk.mineus.mineserver.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Lombok Getter/Setter
public class ServerCreateRequest {

    // 1. 빈 값이나 null, 공백만 있는 경우 차단
    @NotBlank(message = "서버 이름은 필수입니다.")
    // 2. 글자 수 제한
    @Size(min = 2, max = 20, message = "서버 이름은 2~20자 사이여야 합니다.")
    // 3. 특수문자 제한 (예: 알파벳, 숫자, 한글, 공백만 허용)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣 ]+$", message = "서버 이름에 특수문자는 사용할 수 없습니다.")
    private String serverName;
    private Long userId;

    // (나중에 추가될 필드들 예시)
    // @Min(value = 1, message = "최소 1명 이상이어야 합니다.")
    // private int maxPlayers;
}