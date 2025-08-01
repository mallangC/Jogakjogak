package com.zb.jogakjogak.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.Map;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("조각조각 REST API Documentation")
                .description("조각조각 서비스 REST API 문서입니다.\n" +
                        "### 주요 기능\n" +
                        "* 회원 인증 기능\n" +
                        "* 이력서 관련 기능\n" +
                        "* AI 분석 기능\n" +
                        "* Todolist 기능\n\n" +
                        "### 참고 사항\n" +
                        "* 모든 요청은 JWT 인증이 필요합니다.\n" +
                        "* 날짜 형식은 ISO-8601 형식을 사용합니다."
                )
                .version("1.0.0")
                .contact(new Contact()
                        .name("조각조각 개발팀")
                        .email("jogakjogakhelp@gmail.com")));

    }

    @Bean
    public OpenApiCustomizer customOpenApiCustomizer() {
        return new OpenApiCustomizer() {
            @Override
            public void customise(OpenAPI openApi) {
                if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                    openApi.getComponents().getSchemas().remove("Pageable");
                    openApi.getComponents().getSchemas().remove("PageableObject");
                    openApi.getComponents().getSchemas().remove("OpenAIRequestDto");
                    openApi.getComponents().getSchemas().remove("AllGetJDResponseDto");
                    Map<String, Schema> schemas = openApi.getComponents().getSchemas();
                    Iterator<Map.Entry<String, Schema>> iterator = schemas.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<String, Schema> entry = iterator.next();
                        String schemaName = entry.getKey();

                        if (schemaName.contains("HttpApiResponse")) {
                            iterator.remove();
                            continue;
                        }
                    }

                }
            }
        };
    }



}
