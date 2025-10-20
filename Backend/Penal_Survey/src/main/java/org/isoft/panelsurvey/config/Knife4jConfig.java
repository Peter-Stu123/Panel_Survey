package org.isoft.panelsurvey.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置类
 * 用于生成API接口文档
 */
@Configuration
public class Knife4jConfig {

    /**
     * 通过knife4j生成接口文档
     * @return OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Panel Survey 问卷调查系统接口文档")
                        .version("1.0.0")
                        .description("Panel Survey 问卷调查系统的后端API接口文档，提供用户认证、问卷管理、问卷填写、数据分析等功能。"));
    }
}