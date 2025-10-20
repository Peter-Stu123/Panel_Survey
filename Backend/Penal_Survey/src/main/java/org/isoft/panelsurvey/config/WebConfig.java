package org.isoft.panelsurvey.config;

import org.isoft.panelsurvey.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    // 拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 向拦截器注册表（InterceptorRegistry）中添加了一个拦截器 （jwtInterceptor）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                // 指定接口不拦截
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/survey/public/**",
                        // 排除文档相关路径
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**"
                );
    }

    /**
     * 设置静态资源映射，用于Knife4j接口文档访问
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}