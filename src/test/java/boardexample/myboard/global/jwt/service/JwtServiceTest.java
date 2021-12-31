package boardexample.myboard.global.jwt.service;

import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.member.repository.MemberRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Transactional
class JwtServiceTest {



    @Autowired JwtService jwtService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "username";
    private static final String BEARER = "Bearer ";


    private String username = "username";

    @BeforeEach
    public void init(){

        Member member = Member.builder().username(username).password("1234567890").name("Member1").nickName("NickName1").age(22).build();
        memberRepository.save(member);
        clear();
    }

    private void clear(){
        em.flush();
        em.clear();
    }


    @Test
    public void createAccessToken_AccessToken_발급() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(username);

        DecodedJWT verify = JWT.require(Algorithm.HMAC512(secret)).build().verify(accessToken);
        String subject = verify.getSubject();
        String findUsername = verify.getClaim(USERNAME_CLAIM).asString();

        assertThat(findUsername).isEqualTo(username);
        assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
    }

    @Test
    public void createRefreshToken_RefreshToken_발급() throws Exception {
        //given
        String refreshToken = jwtService.createRefreshToken();
        DecodedJWT verify = JWT.require(Algorithm.HMAC512(secret)).build().verify(refreshToken);
        String subject = verify.getSubject();
        String username = verify.getClaim(USERNAME_CLAIM).asString();
        assertThat(subject).isEqualTo(REFRESH_TOKEN_SUBJECT);
        assertThat(username).isNull();
    }

    @Test
    public void updateRefreshToken_refreshToken_업데이트() throws Exception {
        //given
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, refreshToken);
        clear();
        Thread.sleep(3000);

        //when
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, reIssuedRefreshToken);
        clear();

        //then
        assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());//
        assertThat(memberRepository.findByRefreshToken(reIssuedRefreshToken).get().getUsername()).isEqualTo(username);
    }



    @Test
    public void destroyRefreshToken_refreshToken_제거() throws Exception {
        //given
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, refreshToken);
        clear();

        //when
        jwtService.destroyRefreshToken(username);
        clear();

        //then
        assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());

        Member member = memberRepository.findByUsername(username).get();
        assertThat(member.getRefreshToken()).isNull();
    }
    @Test
    public void 토큰_유효성_검사() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        //when, then
        assertThat(jwtService.isTokenValid(accessToken)).isTrue();
        assertThat(jwtService.isTokenValid(refreshToken)).isTrue();
        assertThat(jwtService.isTokenValid(accessToken+"d")).isFalse();
        assertThat(jwtService.isTokenValid(accessToken+"d")).isFalse();

    }


    @Test
    public void setAccessTokenHeader_AccessToken_헤더_설정() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.setAccessTokenHeader(mockHttpServletResponse, accessToken);


        //when
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        //then
        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);

        assertThat(headerAccessToken).isEqualTo(accessToken);
    }

    @Test
    public void setRefreshTokenHeader_RefreshToken_헤더_설정() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.setRefreshTokenHeader(mockHttpServletResponse, refreshToken);


        //when
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        //then
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        assertThat(headerRefreshToken).isEqualTo(refreshToken);
    }





    @Test
    public void sendAccessAndRefreshToken_토큰_전송() throws Exception {
        //given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();


        //when
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);


        //then
        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);



        assertThat(headerAccessToken).isEqualTo(accessToken);
        assertThat(headerRefreshToken).isEqualTo(refreshToken);

    }


    @Test
    public void extractAccessToken_AccessToken_추출() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        //when
        String extractAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(()-> new Exception("토큰이 없습니다"));


        //then
        assertThat(extractAccessToken).isEqualTo(accessToken);
        assertThat(JWT.require(Algorithm.HMAC512(secret)).build().verify(extractAccessToken).getClaim(USERNAME_CLAIM).asString()).isEqualTo(username);
    }


    @Test
    public void extractRefreshToken_RefreshToken_추출() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);


        //when
        String extractRefreshToken = jwtService.extractRefreshToken(httpServletRequest).orElseThrow(()-> new Exception("토큰이 없습니다"));


        //then
        assertThat(extractRefreshToken).isEqualTo(refreshToken);
        assertThat(JWT.require(Algorithm.HMAC512(secret)).build().verify(extractRefreshToken).getSubject()).isEqualTo(REFRESH_TOKEN_SUBJECT);
    }


    private HttpServletRequest setRequest(String accessToken, String refreshToken) throws IOException {

        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        httpServletRequest.addHeader(accessHeader, BEARER+headerAccessToken);
        httpServletRequest.addHeader(refreshHeader, BEARER+headerRefreshToken);

        return httpServletRequest;
    }


    @Test
    public void extractUsername_Username_추출() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String requestAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(()->new Exception("토큰이 없습니다"));

        //when
        String extractUsername = jwtService.extractUsername(requestAccessToken).orElseThrow(()->new Exception("토큰이 없습니다"));


        //then
        assertThat(extractUsername).isEqualTo(username);
    }




}