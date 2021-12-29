package boardexample.myboard.global.login.filter;

import boardexample.myboard.global.login.filter.dto.UsernamePasswordDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class JsonUsernamePasswordLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_LOGIN_REQUEST_URL_PREFIX = "/login";  // /login/oauth2/ + ????? 로 오는 요청을 처리할 것이다

    private static final String HTTP_METHOD = "POST";    //HTTP 메서드의 방식은 POST 이다.

    private static final String CONTENT_TYPE = "application/json";//json 타입의 데이터로만 로그인을 진행한다.



    private final ObjectMapper objectMapper;



    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL_PREFIX, HTTP_METHOD); //=>   /login 의 요청에, POST로 온 요청에 매칭된다.

    public JsonUsernamePasswordLoginFilter(ObjectMapper objectMapper,
                                          AuthenticationManager authenticationManager) {

        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);   // 위에서 설정한  /oauth2/login/* 의 요청에, GET으로 온 요청을 처리하기 위해 설정한다.

        this.setAuthenticationManager(authenticationManager);//ProviderManager를 지정해 주어야 한다.
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if(!request.getContentType().equals(CONTENT_TYPE)) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }

        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);


        UsernamePasswordDto usernamePasswordDto = objectMapper.readValue(messageBody, UsernamePasswordDto.class);


        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(usernamePasswordDto.getUsername(), usernamePasswordDto.getPassword());//principal 과 credentials 전달

        return this.getAuthenticationManager().authenticate(authRequest);
    }







}
