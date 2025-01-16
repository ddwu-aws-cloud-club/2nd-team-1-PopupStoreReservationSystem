package com.westsomsom.finalproject.global.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // HTTP 응답 객체 변환
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 기존 필터 체인 실행
        chain.doFilter(request, response);

        // ALB에서 301을 리턴하는 경우 이를 308로 변경
        if (httpServletResponse.getStatus() == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            httpServletResponse.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            httpServletResponse.setHeader("Location", httpServletResponse.getHeader("Location")); // 리디렉트 주소 유지
        }
    }
}
