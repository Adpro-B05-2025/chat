package id.ac.ui.cs.advprog.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MessageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleMessageNotFoundException(MessageNotFoundException ex) {
        return ex.getMessage();
    }
}