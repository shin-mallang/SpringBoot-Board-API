package boardexample.myboard.global.login;

import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.member.Role;
import boardexample.myboard.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

    @Autowired MockMvc mockMvc;

    @Autowired MemberRepository memberRepository;

    @Autowired EntityManager em;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    ObjectMapper objectMapper = new ObjectMapper();

    private static String USERNAME = "username";
    private static String PASSWORD = "123456789";
    private static String LOGIN_RUL = "/login";

    private static String LOGIN_SUCCESS_MESSAGE = "success";
    private static String LOGIN_FAIL_MESSAGE = "fail";


    private void clear(){
        em.flush();
        em.clear();
    }

    @BeforeEach
    private void init(){

            memberRepository.save(Member.builder().username(USERNAME).password(delegatingPasswordEncoder.encode(PASSWORD)).name("Member1").nickName("NickName1").role(Role.USER).age(22).build());
            clear();
    }

    /**
     * 오류 - /login에 POST가 아닌 다른 요청 보내기
     * 오류 - /login에 JSON이 아닌 다른 요청 보내기
     * 오류 - /login이 아닌 곳에 요청 보내기
     *
     * /login 실패
     * /login 성공
     *
     */


    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    @Test
    public void 로그인_성공() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD);


        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();


        //then
        assertThat(result.getResponse().getHeader(accessHeader)).isNotNull();
        assertThat(result.getResponse().getHeader(refreshHeader)).isNotNull();


    }


    @Test
    public void 로그인_실패_아이디틀림() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME+"123");
        map.put("password",PASSWORD);


        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();


        //then
        assertThat(result.getResponse().getHeader(accessHeader)).isNull();
        assertThat(result.getResponse().getHeader(refreshHeader)).isNull();

    }

    @Test
    public void 로그인_실패_비밀번호틀림() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD+"123");


        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();


        //then
        assertThat(result.getResponse().getHeader(accessHeader)).isNull();
        assertThat(result.getResponse().getHeader(refreshHeader)).isNull();

    }


    @Test
    public void 로그인_주소가_틀리면_FORBIDDEN() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD);


        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL+"123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    /**
     * if(!request.getContentType().equals(CONTENT_TYPE)) {
     *             throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
     *         }
     *         에 걸리므로 로그인실패 Handler에 걸린다 따라서 200 상태코드, fail
     */
    @Test
    public void 로그인_데이터형식_JSON이_아니면_200에_fail() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD);


        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(LOGIN_FAIL_MESSAGE);
    }


    @Test
    public void 로그인_HTTP_METHOD_GET이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD);


        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    public void 오류_로그인_HTTP_METHOD_PUT이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD);


        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


}
