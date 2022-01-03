package boardexample.myboard.domain.member.exception;

import boardexample.myboard.global.exception.BaseException;
import boardexample.myboard.global.exception.BaseExceptionType;

public class MemberException extends BaseException {
    private BaseExceptionType exceptionType;


    public MemberException(BaseExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return exceptionType;
    }
}
