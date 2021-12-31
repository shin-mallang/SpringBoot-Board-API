package boardexample.myboard.global.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public interface JwtService {


    String createAccessToken(String username);
    String createRefreshToken();

    void updateRefreshToken(String username, String refreshToken);

    void destroyRefreshToken(String username);


    void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);
    void sendAccessToken(HttpServletResponse response, String accessToken);


    Optional<String> extractAccessToken(HttpServletRequest request);

    Optional<String> extractRefreshToken(HttpServletRequest request);

    Optional<String> extractUsername(String accessToken);



    void setAccessTokenHeader(HttpServletResponse response, String accessToken);
    void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);
}
