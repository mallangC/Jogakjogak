package com.zb.jogakjogak.global.config;

import com.zb.jogakjogak.ga.interceptor.GaApiCallInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final GaApiCallInterceptor gaApiCallInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // '/jds/'로 시작하는 모든 요청에 인터셉터 적용
        registry.addInterceptor(gaApiCallInterceptor).addPathPatterns("/jds/**");
        // '/member/'로 시작하는 모든 요청에 인터셉터 적용
        registry.addInterceptor(gaApiCallInterceptor).addPathPatterns("/member/**");
    }
}
