package snghk.mineus.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // 모든 컨트롤러에서 발생하는 에러를 여기서 잡습니다.
public class GlobalExceptionHandler {

    // @Valid 검증 실패 시 발생하는 예외를 잡음
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // 에러가 난 필드와 메시지를 뽑아서 Map으로 만듦
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // 400 Bad Request와 함께 깔끔한 JSON 반환
        return ResponseEntity.badRequest().body(errors);
    }
}