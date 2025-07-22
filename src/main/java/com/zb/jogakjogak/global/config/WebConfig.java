package com.zb.jogakjogak.global.config;

import com.zb.jogakjogak.ga.interceptor.GaApiCallInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final GaApiCallInterceptor gaApiCallInterceptor;
    
    @Value("${google.analytics.measurementId:}")
    private String measurementId;

    @Value("${google.analytics.apiSecret:}")
    private String apiSecret;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // GA가 활성화된 경우에만 인터셉터 등록
        if (measurementId != null && !measurementId.trim().isEmpty() 
            && apiSecret != null && !apiSecret.trim().isEmpty()) {
            // '/jds/'로 시작하는 모든 요청에 인터셉터 적용
            registry.addInterceptor(gaApiCallInterceptor).addPathPatterns("/jds/**");
            // '/member/'로 시작하는 모든 요청에 인터셉터 적용
            registry.addInterceptor(gaApiCallInterceptor).addPathPatterns("/member/**");
        }
    }
}
