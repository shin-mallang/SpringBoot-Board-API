package boardexample.myboard.global.jwt.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface JwtService {


    String createAccessToken(String username);
    String createRefreshToken();

    void updateRefreshToken(String username, String refreshToken);

    void destroyRefreshToken(String username);


    void sendToken(HttpServletResponse response, String accessToken, String refreshToken) throws IOException;

}
