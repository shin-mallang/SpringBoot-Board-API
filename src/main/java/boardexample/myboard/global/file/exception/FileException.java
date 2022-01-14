package boardexample.myboard.global.file.exception;

import boardexample.myboard.global.exception.BaseException;
import boardexample.myboard.global.exception.BaseExceptionType;

public class FileException extends BaseException {
    private BaseExceptionType exceptionType;


    public FileException(BaseExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return exceptionType;
    }
}
