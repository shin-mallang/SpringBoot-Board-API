package boardexample.myboard.domain.member.dto;

import boardexample.myboard.domain.member.Member;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


public record MemberSignUpDto(@NotBlank(message = "아이디를 입력해주세요") @Size(min = 7, max = 25, message = "아이디는 7~25자 내외로 입력해주세요")
                              String username,

                              @NotBlank(message = "비밀번호를 입력해주세요")
                              @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,30}$",
                                      message = "비밀번호는 8~30 자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다.")
                              String password,

                              @NotBlank(message = "이름을 입력해주세요") @Size(min=2, message = "사용자 이름이 너무 짧습니다.")
                              @Pattern(regexp = "^[A-Za-z가-힣]+$", message = "사용자 이름은 한글 또는 알파벳만 입력해주세요.")
                              String name,

                              @NotBlank(message = "닉네임을 입력해주세요.")
                              @Size(min=2, message = "닉네임이 너무 짧습니다.")
                              @NotBlank String nickName,


                              @NotNull(message = "나이를 입력해주세요")
                              @Range(min = 0, max = 150)
                              Integer age) {

    public Member toEntity() {
        return Member.builder().username(username).password(password).name(name).nickName(nickName).age(age).build();
    }
}
