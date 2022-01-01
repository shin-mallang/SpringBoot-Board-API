package boardexample.myboard.domain.member.dto;

import boardexample.myboard.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MemberSignUpDto {

    private String username;
    private String password;
    private String name;
    private String nickName;
    private Integer age;



    public Member toEntity() {
        return Member.builder().username(username).password(password).name(name).nickName(nickName).age(age).build();
    }
}
