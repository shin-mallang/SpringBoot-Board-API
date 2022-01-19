package boardexample.myboard.global.jwt.filter;

import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.member.Role;
import boardexample.myboard.domain.member.repository.MemberRepository;
import boardexample.myboard.global.jwt.service.JwtService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JwtFilterAuthenticationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Autowired
    JwtService jwtService;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();


    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static String KEY_USERNAME = "username";
    private static String KEY_PASSWORD = "password";
    private static String USERNAME = "username";
    private static String PASSWORD = "123456789";

    private static String LOGIN_RUL = "/login";


    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String BEARER = "Bearer ";


    private ObjectMapper objectMapper = new ObjectMapper();



    private void clear(){
        em.flush();
        em.clear();
    }


    @BeforeEach
    private void init(){
        memberRepository.save(Member.builder().username(USERNAME).password(delegatingPasswordEncoder.encode(PASSWORD)).name("Member1").nickName("NickName1").role(Role.USER).age(22).build());
        clear();
    }



    private Map getUsernamePasswordMap(String username, String password){
        Map<String, String> map = new HashMap<>();
        map.put(KEY_USERNAME, username);
        map.put(KEY_PASSWORD, password);
        return map;
    }


    private Map getAccessAndRefreshToken() throws Exception {

        Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);

        MvcResult result = mockMvc.perform(
                         post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);
        String refreshToken = result.getResponse().getHeader(refreshHeader);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(accessHeader,accessToken);
        tokenMap.put(refreshHeader,refreshToken);

        return tokenMap;
    }






    /**
     * AccessToken : 존재하지 않음,
     * RefreshToken : 존재하지 않음
     */
    @Test
    public void Access_Refresh_모두_존재_X() throws Exception {
        //when, then
        mockMvc.perform(get(LOGIN_RUL+"123"))//login이 아닌 다른 임의의 주소
                .andExpect(status().isForbidden());
    }



    /**
     * AccessToken : 유효,
     * RefreshToken : 존재하지 않음
     */
    @Test
    public void AccessToken만_보내서_인증() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);

        //when, then
        mockMvc.perform(get(LOGIN_RUL+"123").header(accessHeader,BEARER+ accessToken))//login이 아닌 다른 임의의 주소
                .andExpectAll(status().isNotFound());//없는 주소로 보냈으므로 NotFound

    }


    /**
     * AccessToken : 유효하지 않음,
     * RefreshToken : 존재하지 않음
     */
    @Test
    public void 안유효한AccessToken만_보내서_인증X_상태코드는_403() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);

        //when, then
        mockMvc.perform(get(LOGIN_RUL+"123").header(accessHeader,accessToken+"1"))//login이 아닌 다른 임의의 주소
                .andExpectAll(status().isForbidden());//없는 주소로 보냈으므로 NotFound
    }


    /**
     * AccessToken : 존재하지 않음
     * RefreshToken : 유효
     */
    @Test
    public void 유효한RefreshToken만_보내서_AccessToken_재발급_200() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String refreshToken= (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123").header(refreshHeader, BEARER+refreshToken))//login이 아닌 다른 임의의 주소
                .andExpect(status().isOk()).andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);

        String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(accessToken).getSubject();
        assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
    }



    /**
     * AccessToken : 존재하지 않음
     * RefreshToken : 유효하지 않음
     */
    @Test
    public void 안유효한RefreshToken만_보내면_403() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String refreshToken= (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
         mockMvc.perform(get(LOGIN_RUL + "123").header(refreshHeader, refreshToken))//Bearer을 붙이지 않음
                .andExpect(status().isForbidden());

        mockMvc.perform(get(LOGIN_RUL + "123").header(refreshHeader, BEARER+refreshToken+"1"))//유효하지 않은 토큰
                .andExpect(status().isForbidden());
    }



    /**
     * AccessToken : 유효
     * RefreshToken : 유효
     */
    @Test
    public void 유효한RefreshToken이랑_유효한AccessToken_같이보냈을때_AccessToken_재발급_200() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken= (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123")
                        .header(refreshHeader, BEARER + refreshToken)
                        .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseAccessToken = result.getResponse().getHeader(accessHeader);
        String responseRefreshToken = result.getResponse().getHeader(refreshHeader);

        String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(responseAccessToken).getSubject();

        assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
        assertThat(responseRefreshToken).isNull();//refreshToken은 재발급되지 않음
    }





    /**
     * AccessToken : 유효하지 않음
     * RefreshToken : 유효
     */
    @Test
    public void 유효한RefreshToken이랑_안유효한AccessToken_같이보냈을때_AccessToken_재발급_200() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken= (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123")
                        .header(refreshHeader, BEARER + refreshToken)
                        .header(accessHeader, BEARER + accessToken + 1))
                .andExpect(status().isOk())
                .andReturn();

        String responseAccessToken = result.getResponse().getHeader(accessHeader);
        String responseRefreshToken = result.getResponse().getHeader(refreshHeader);

        String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(responseAccessToken).getSubject();

        assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
        assertThat(responseRefreshToken).isNull();//refreshToken은 재발급되지 않음
    }


    /**
     * AccessToken : 유효
     * RefreshToken : 유효하지 않음
     */
    @Test
    public void 안유효한RefreshToken이랑_유효한AccessToken_같이보냈을때_상태코드200_혹은404_RefreshToken은_AccessToken모두_재발급되지않음() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken= (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123")
                        .header(refreshHeader, BEARER + refreshToken+1)
                        .header(accessHeader, BEARER + accessToken ))
                .andExpect(status().isNotFound())//없는 주소로 보냈으므로 NotFound
                .andReturn();

        String responseAccessToken = result.getResponse().getHeader(accessHeader);
        String responseRefreshToken = result.getResponse().getHeader(refreshHeader);


        assertThat(responseAccessToken).isNull();//accessToken은 재발급되지 않음
        assertThat(responseRefreshToken).isNull();//refreshToken은 재발급되지 않음
    }



    /**
     * AccessToken : 유효하지 않음
     * RefreshToken : 유효하지 않음
     */
    @Test
    public void 안유효한RefreshToken이랑_안유효한AccessToken_같이보냈을때_403() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken= (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123")
                        .header(refreshHeader, BEARER + refreshToken+1)
                        .header(accessHeader, BEARER + accessToken+1 ))
                .andExpect(status().isForbidden())//없는 주소로 보냈으므로 NotFound
                .andReturn();

        String responseAccessToken = result.getResponse().getHeader(accessHeader);
        String responseRefreshToken = result.getResponse().getHeader(refreshHeader);


        assertThat(responseAccessToken).isNull();//accessToken은 재발급되지 않음
        assertThat(responseRefreshToken).isNull();//refreshToken은 재발급되지 않음

    }

    @Test
    public void 로그인_주소로_보내면_필터작동_X() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken= (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(post(LOGIN_RUL)  //get인 경우 config에서 permitAll을 했기에 notFound
                        .header(refreshHeader, BEARER + refreshToken)
                        .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isBadRequest())
                .andReturn();

    }



}
