package handler;

import exception.BadrequestExeption;
import exception.ErrorResponse;
import exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundExeception(NotFoundException ex){
        ErrorResponse response = ErrorResponse.builder().message(ex.getMessage()).status(HttpStatus.NOT_FOUND.value()).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccesDenided(AccessDeniedException ex){
        ErrorResponse response = ErrorResponse.builder().message(ex.getMessage()).status(HttpStatus.FORBIDDEN.value()).build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExeception(Exception ex){
        ErrorResponse response = ErrorResponse.builder().message(ex.getMessage()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(BadrequestExeption.class)
    public ResponseEntity<ErrorResponse> handleExeception(BadrequestExeption ex){
        ErrorResponse response = ErrorResponse.builder().message(ex.getMessage()).status(HttpStatus.BAD_REQUEST.value()).build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
