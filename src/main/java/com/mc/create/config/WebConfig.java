package com.mc.create.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 前端是 file:// 打开的静态页面，属于跨域请求，必须放开 CORS，
 * 否则浏览器 fetch 会被拦截，后端一个请求都收不到。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")       // 允许任意源（本地开发）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // 导出接口要把文件名交给前端（下载时用项目名命名）
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                // 注册/登录/退出不需要（也无法）带有效 token：
                // 前端退出时会先清本地会话再调 /auth/logout，若这里拦截会返回 401
                .excludePathPatterns("/api/auth/register", "/api/auth/login", "/api/auth/logout");
    }
}
