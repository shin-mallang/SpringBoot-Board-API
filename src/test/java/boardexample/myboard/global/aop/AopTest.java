package boardexample.myboard.global.aop;

import boardexample.myboard.domain.member.dto.MemberSignUpDto;
import boardexample.myboard.domain.member.service.MemberServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

@SpringBootTest
public class AopTest {

    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    Method helloMethod;



    @BeforeEach
    public void init() throws NoSuchMethodException {
        helloMethod = MemberServiceImpl.class.getMethod("signUp", MemberSignUpDto.class);
    }

    @Test
    public void test() throws Exception {
        pointcut.setExpression("execution(* boardexample.myboard.domain.member.*.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();

    }
    @Test
    public void test2() throws Exception {
        pointcut.setExpression("execution(* boardexample.myboard.domain.member..*.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();



    }
    @Test
    public void test3() throws Exception {

        pointcut.setExpression("execution(* boardexample.myboard.domain..*Service*.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();

        //TODO 왜 중복으로 setExpression 하면 적용이 안될까?
        /*pointcut.setExpression("execution(* boardexample.myboard.domain..*service.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();*/
    }






}
