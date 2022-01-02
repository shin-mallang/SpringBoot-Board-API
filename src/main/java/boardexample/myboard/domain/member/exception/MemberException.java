package boardexample.myboard.domain.member.exception;

import lombok.Getter;

@Getter
public class MemberException extends RuntimeException{

    private MemberTypeException memberTypeException;
}
