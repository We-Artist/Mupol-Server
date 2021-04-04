package com.mupol.mupolserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.sql.Timestamp;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("Mupol-Server")
                .description("API documentation\n뮤폴 서비스 화이팅 🔥")
                .version("0.0.1")
                .build();
    }

    @Bean
    public Docket commonApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.any()) // 현재 RequestMapping으로 할당된 모든 URL 리스트를 추출
                .paths(PathSelectors.ant("/api/**")) // 그중 /api/** 인 URL들만 필터링
                .build()
                .directModelSubstitute(Timestamp.class, Long.class); // 예시에 보이는 날짜 포맷을 자연스럽게 만들어 줌
    }
}
