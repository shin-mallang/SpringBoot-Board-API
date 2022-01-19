package boardexample.myboard.learning;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PasswordEncoderTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    private static String PASSWROD = "신동훈ShinDongHun";

    @Test
    public void 패스워드_암호화() throws Exception {

        //when
        String encodePassword = passwordEncoder.encode(PASSWROD);

        //then
        assertThat(encodePassword).startsWith("{");
        assertThat(encodePassword).contains("{bcrypt}");
        assertThat(encodePassword).isNotEqualTo(PASSWROD);

    }

    @Test
    public void 패스워드_랜덤_암호화() throws Exception {

        //when
        String encodePassword = passwordEncoder.encode(PASSWROD);
        String encodePassword2 = passwordEncoder.encode(PASSWROD);

        //then
        assertThat(encodePassword).isNotEqualTo(encodePassword2);

    }


    @Test
    public void 암호화된_비밀번호_매치() throws Exception {

        //when
        String encodePassword = passwordEncoder.encode(PASSWROD);

        //then
        assertThat(passwordEncoder.matches(PASSWROD, encodePassword)).isTrue();

    }
}
