package snghk.mineus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 이 예외가 발생하면 409 Conflict 상태 코드를 반환하도록 설정
@ResponseStatus(HttpStatus.CONFLICT)
public class ServerLimitExceededException extends RuntimeException {
    public ServerLimitExceededException(String message) {
        super(message);
    }
}