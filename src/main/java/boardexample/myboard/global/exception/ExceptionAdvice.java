package boardexample.myboard.global.exception;

import boardexample.myboard.domain.member.exception.MemberException;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {


    @ExceptionHandler(Exception.class)
    public ResponseEntity handleMemberEx(Exception exception){

        ExceptionDto exceptionDto = new ExceptionDto();
        //exceptionDto.setErrorCode(memberException.getMemberTypeException().getErrorCode());
        return new ResponseEntity(exceptionDto, HttpStatus.OK);
    }


    @Data
    class ExceptionDto{
        private Integer errorCode;


    }
}
