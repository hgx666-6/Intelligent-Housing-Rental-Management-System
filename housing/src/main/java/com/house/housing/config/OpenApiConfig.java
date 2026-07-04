package com.house.housing.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("房屋租凭系统API文档")
                        .version("1.0.0")
                        .description("接口描述")
                        .contact(new Contact()
                                .name("小组")
                                .email("xxx@qq.com")
                                .url("https://github.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("完整文档")
                        .url("https://xxx.com/docs"));
    }
}