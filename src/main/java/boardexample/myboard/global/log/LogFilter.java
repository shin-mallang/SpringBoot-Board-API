package boardexample.myboard.global.log;


import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;


@Slf4j
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;


        StringBuilder sb = new StringBuilder();

        sb.append(String.format("\nTIME: [%s]\nURI: [%s]\nHttpMethod: [%s]", LocalDateTime.now(),
                httpServletRequest.getRequestURI(),
                httpServletRequest.getMethod()));

        sb.append("\n\n===== locale =====\n");
        httpServletRequest.getLocales().asIterator().forEachRemaining(locale -> sb.append(locale+", "));



        sb.append("\n============ Header Info ==========\n");
        httpServletRequest.getHeaderNames().asIterator().forEachRemaining(name -> sb.append(String.format("%s : %s\n",name,httpServletRequest.getHeader(name))));


        log.info("{}", sb);
        chain.doFilter(request, response);
    }
}
