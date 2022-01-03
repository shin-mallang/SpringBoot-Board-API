package boardexample.myboard.domain.member.exception;

import boardexample.myboard.global.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum MemberExceptionType implements BaseExceptionType {
    //== 회원가입, 로그인 시 ==//
    ALREADY_EXIST_USERNAME(600, HttpStatus.OK, "이미 존재하는 아이디입니다."),
    LOGIN_FAIL(601, HttpStatus.OK, "로그인에 실패."),
    MUST_REGISTER(603, HttpStatus.OK, "회원가입을 진행해야 합니다."),
    WRONG_PASSWORD(605,HttpStatus.OK, "비밀번호가 잘못되었습니다."),
    NOT_FOUND_MEMBER(606, HttpStatus.OK, "회원 정보가 없습니다.");




    private int errorCode;
    private HttpStatus httpStatus;
    private String errorMessage;

    MemberExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    @Override
    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
