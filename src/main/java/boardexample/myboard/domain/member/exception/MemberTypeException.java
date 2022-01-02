package boardexample.myboard.domain.member.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberTypeException {

    NULL_FIELD_EXCEPTION(1000, "회원가입 요청 중 채워지지 않은 필드가 있습니다", HttpStatus.BAD_REQUEST);



    private Integer errorCode;
    private String cause;
    private HttpStatus httpStatus;

    MemberTypeException(int errorCode, String cause, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.cause = cause;
        this.httpStatus = httpStatus;
    }
}
