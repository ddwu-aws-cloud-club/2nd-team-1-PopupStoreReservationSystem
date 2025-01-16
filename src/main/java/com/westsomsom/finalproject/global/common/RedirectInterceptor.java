package com.westsomsom.finalproject.global.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RedirectInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (response.getStatus() == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        }
    }
}