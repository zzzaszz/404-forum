package zzz.service.forum.config.knife4j;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Knife4j 配置类，用于生成 RESTful API 文档
 * 兼容 Spring Boot 2.5.3
 * <p>
 * 注意：已禁用 Swagger UI，避免在生产环境暴露 API 文档
 */

@Configuration("wvKnife4jConfig")
@EnableKnife4j
public class Knife4jConfig {

     private final static Logger log = LoggerFactory.getLogger(Knife4jConfig.class);
     

    @Resource
    private ApplicationContext applicationContext;

    @Bean(name = "defaultApi")
    public Docket defaultApi() {

        // 获取项目主包路径
        String mainPackage = getMainApplicationPackage();
        log.info("📦 [KNIFE4J] 检测到的主包路径: {}", mainPackage);

        // 使用正确的 Predicate 类型
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title(mainPackage + " 项目文档")
                        .description(StringUtils.isNotBlank(mainPackage) ? " + " + mainPackage + " 项目接口" : "")
                        .version("1.0.0")
                        .build())
                .select()
                .apis(buildApiSelector(mainPackage))
                .paths(PathSelectors.any())
                .build()
                // 注册 SecuritySchemes，用于支持 Authorization 头
                .securitySchemes(Collections.singletonList(apiAuth()))
                // 注册 SecurityContexts，用于将 SecurityScheme 应用到 API 路径
                .securityContexts(Collections.singletonList(securityContext()));

        log.info("📋 [KNIFE4J] API 文档标题: {} 项目文档", mainPackage);
        return docket;
    }

    /**
     * 构建API选择器，支持多包扫描
     *
     * @param mainPackage 项目主包路径
     * @return API选择器
     */
    private Predicate<RequestHandler> buildApiSelector(String mainPackage) {
        Predicate<RequestHandler> selector = RequestHandlerSelectors.basePackage("inks.sa.common.core");

        if (StringUtils.isNotBlank(mainPackage)) {
            selector = selector.or(RequestHandlerSelectors.basePackage(mainPackage));
        }

        return selector;
    }

    /**
     * 自动获取项目主包路径
     * 通过分析Spring上下文中的主启动类来推导项目根包
     *
     * @return 项目主包路径，如果无法确定则返回null
     */
    private String getMainApplicationPackage() {
        log.info("🔍 [KNIFE4J] 开始自动检测项目主包路径...");

        try {
            // 获取所有带@SpringBootApplication注解的Bean
            Map<String, Object> beans = applicationContext.getBeansWithAnnotation(
                    org.springframework.boot.autoconfigure.SpringBootApplication.class);

            log.info("📦 [KNIFE4J] 找到 {} 个 @SpringBootApplication Bean", beans.size());

            for (Object bean : beans.values()) {
                String beanPackage = bean.getClass().getPackage().getName();
                // 排除sa-common自身的启动类
                if (!beanPackage.startsWith("inks.sa.common.core")) {
                    log.info("✅ [KNIFE4J] 选择主包: {}", beanPackage);
                    return beanPackage;
                }
            }

            // 如果没有找到外部启动类，尝试通过当前配置类推导
            String currentPackage = this.getClass().getPackage().getName();
            log.debug("🔍 [KNIFE4J] 当前配置类包: {}", currentPackage);

            // 从 inks.service.std.sale.config 推导到项目包 inks.service.std.sale
            if (currentPackage.contains(".config")) {
                String derivedPackage = currentPackage.substring(0, currentPackage.lastIndexOf(".config"));
                log.info("🔍 [KNIFE4J] 推导出的包: {}", derivedPackage);
                return derivedPackage;
            }

        } catch (Exception e) {
            log.error("❌ [KNIFE4J] 检测主包路径时发生异常: {}", e.getMessage(), e);
        }

        String defaultPackage = "inks.service.std.sale";
        log.info("🔧 [KNIFE4J] 使用默认包: {}", defaultPackage);
        return defaultPackage; // 默认返回当前项目包
    }

    /**
     * 定义用于 Authorization 的 ApiKey
     *
     * @return ApiKey 对象
     */
    private SecurityScheme apiAuth() {
        return new ApiKey("Authorization", "Authorization", "header");
    }

    /**
     * 配置 SecurityContext，将 ApiKey 与请求路径关联
     *
     * @return SecurityContext 对象
     */
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                // 配置默认的 SecurityReference
                .securityReferences(defaultAuth())
                // 将 ApiKey 作用于所有路径
                .forPaths(PathSelectors.any())
                .build();
    }

    /**
     * 配置默认的 SecurityReference
     *
     * @return SecurityReference 列表
     */
    private List<SecurityReference> defaultAuth() {
        // 定义全局授权范围
        SecurityReference securityReference = new SecurityReference(
                "Authorization",
                new springfox.documentation.service.AuthorizationScope[]{
                        // 授权范围为全局
                        new springfox.documentation.service.AuthorizationScope("global", "accessEverything")
                }
        );
        return Collections.singletonList(securityReference);
    }
}
