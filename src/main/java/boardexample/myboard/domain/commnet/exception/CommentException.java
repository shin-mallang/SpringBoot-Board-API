package boardexample.myboard.domain.commnet.exception;

import boardexample.myboard.global.exception.BaseException;
import boardexample.myboard.global.exception.BaseExceptionType;

public class CommentException extends BaseException {

    private BaseExceptionType baseExceptionType;


    public CommentException(BaseExceptionType baseExceptionType) {
        this.baseExceptionType = baseExceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return this.baseExceptionType;
    }
}
