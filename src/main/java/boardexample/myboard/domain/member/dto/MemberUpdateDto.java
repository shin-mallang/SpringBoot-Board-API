package boardexample.myboard.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
public class MemberUpdateDto {

    private final Optional<String> name;
    private final Optional<String> nickName;
    private final Optional<Integer> age;

    @Builder
    public MemberUpdateDto(String name, String nickName, Integer age) {
        this.name = Optional.ofNullable(name);
        this.nickName = Optional.ofNullable(nickName);
        this.age = Optional.ofNullable(age);
    }
}
