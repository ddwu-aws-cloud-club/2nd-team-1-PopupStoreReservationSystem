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

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        chain.doFilter(request, response);

        // ALB가 반환한 301을 감지하여 308으로 변경
        if (httpServletResponse.getStatus() == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            httpServletResponse.setStatus(308);
        }
    }
}