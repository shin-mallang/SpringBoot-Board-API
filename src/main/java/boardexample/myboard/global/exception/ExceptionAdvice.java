package boardexample.myboard.global.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {


    //HttpMessageNotReadableException  => json 파싱 오류

    @ExceptionHandler(BaseException.class)
    public ResponseEntity handleBaseEx(BaseException exception){
        log.error("BaseException errorMessage(): {}",exception.getExceptionType().getErrorMessage());
        log.error("BaseException errorCode(): {}",exception.getExceptionType().getErrorCode());

        return new ResponseEntity(new ExceptionDto(exception.getExceptionType().getErrorCode()),exception.getExceptionType().getHttpStatus());
    }



    //@Valid 에서 예외 발생
    @ExceptionHandler(BindException.class)
    public ResponseEntity handleValidEx(BindException exception){

        log.error("@ValidException 발생! {}", exception.getMessage() );
        return new ResponseEntity(new ExceptionDto(2000),HttpStatus.BAD_REQUEST);
    }

    //HttpMessageNotReadableException  => json 파싱 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity httpMessageNotReadableExceptionEx(HttpMessageNotReadableException exception){

        log.error("Json을 파싱하는 과정에서 예외 발생! {}", exception.getMessage() );
        return new ResponseEntity(new ExceptionDto(3000),HttpStatus.BAD_REQUEST);
    }






    @ExceptionHandler(Exception.class)
    public ResponseEntity handleMemberEx(Exception exception) {


        exception.printStackTrace();
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }


    @Data
    @AllArgsConstructor
    static class ExceptionDto {
        private Integer errorCode;
    }
}
