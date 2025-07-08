package com.zb.jogakjogak.global.config;

import com.zb.jogakjogak.ga.interceptor.GaApiCallInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GaApiCallInterceptor gaApiCallInterceptor;

    public WebConfig(GaApiCallInterceptor gaApiCallInterceptor) {
        this.gaApiCallInterceptor = gaApiCallInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /api/로 시작하는 모든 요청에 인터셉터 적용
        registry.addInterceptor(gaApiCallInterceptor).addPathPatterns("/api/**");
    }
}
