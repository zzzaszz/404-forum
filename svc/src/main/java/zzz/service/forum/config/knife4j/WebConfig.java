package zzz.service.forum.config.knife4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zzz.service.forum.interceptor.AdminInterceptor;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Web 配置类，处理静态资源映射和 Knife4j 认证
 * 合并了原 WebConfig 和 Knife4jAuthConfig 的功能
 */
@Configuration("wvWebConfig")
@ServletComponentScan
@WebFilter(urlPatterns = {"/doc.html", "/webjars/*", "/swagger-resources/*", "/v2/api-docs/*"})
public class WebConfig implements WebMvcConfigurer, Filter {
    private final static Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Autowired
    private AdminInterceptor adminInterceptor;

    // Knife4j 认证信息
    private static final String USERNAME = "inks";
    private static final String PASSWORD = "8866";

    // 需要保护的路径
    private static final Set<String> PROTECTED_PATHS = new HashSet<>(Arrays.asList(
            "/doc.html",
            "/webjars/",
            "/swagger-resources/",
            "/v2/api-docs/"
    ));



    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Knife4j 资源处理
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        // 添加自定义静态资源处理
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA fallback: 将前端路由转发到 index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理员权限拦截：用户管理、板块管理接口仅管理员可访问
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/user/**", "/board/**");
    }

    // ========== Knife4j 认证过滤器实现 ==========

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String remoteAddr = httpRequest.getRemoteAddr();
        log.info("🌐 [AUTH] 收到请求: {} {} from {}", method, path, remoteAddr);
        // 检查是否需要认证
        boolean requiresAuth = PROTECTED_PATHS.stream()
                .anyMatch(protectedPath -> path.startsWith(protectedPath));
        log.debug("🔐 [AUTH] 路径 {} 需要认证: {}", path, requiresAuth);

        if (!requiresAuth) {
            chain.doFilter(request, response);
            return;
        }

        // 处理认证
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String encodedCredentials = authHeader.substring(6);
            String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
            String[] credentials = decodedCredentials.split(":", 2);

            if (credentials.length == 2 &&
                    USERNAME.equals(credentials[0]) &&
                    PASSWORD.equals(credentials[1])) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 认证失败，返回 401
        log.warn("🚫 [AUTH] 返回 401 未授权响应");
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Knife4j API Documentation\"");
    }

    @Override
    public void destroy() {
        // 清理操作
    }
}
