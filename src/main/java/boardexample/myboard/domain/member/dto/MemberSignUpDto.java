package boardexample.myboard.domain.member.dto;

import boardexample.myboard.domain.member.Member;
import lombok.Data;


public record MemberSignUpDto(String username, String password, String name,
                              String nickName, Integer age) {

    public Member toEntity() {
        return Member.builder().username(username).password(password).name(name).nickName(nickName).age(age).build();
    }
}
