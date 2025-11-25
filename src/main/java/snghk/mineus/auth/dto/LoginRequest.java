package snghk.mineus.auth.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
