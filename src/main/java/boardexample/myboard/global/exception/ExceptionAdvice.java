package boardexample.myboard.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {


    @ExceptionHandler(Exception.class)
    public ResponseEntity handleMemberEx(Exception exception){
        return new ResponseEntity(HttpStatus.OK);
    }

}
