package com.westsomsom.finalproject.global.config;

import com.westsomsom.finalproject.global.common.RedirectInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RedirectInterceptor redirectInterceptor;

    public WebConfig(RedirectInterceptor redirectInterceptor) {
        this.redirectInterceptor = redirectInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(redirectInterceptor);
    }
}