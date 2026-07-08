package zzz.service.forum.config.knife4j;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import static springfox.documentation.spi.DocumentationType.OAS_30;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

/**
 * Springfox 插件：自动优化接口显示格式
 * 1. 左侧菜单项显示为路径格式（如：SaMenuApp/create）
 * 2. 一级分组显示为：控制器名 + 原@Api注解描述（如：SaMenuApp 通用:APP菜单）
 *
 * @author nanno
 */
@Component("wvKnife4jPlugin")
@Order(1000)
public class Knife4jPlugin implements OperationBuilderPlugin {

    @Override
    public void apply(OperationContext context) {
        // 1) 菜单项：方法名 + 原接口中文注释
        String rawPath = context.requestMappingPattern();              // 例：/SaWebNav/create
        String methodName = "";
        if (rawPath != null && rawPath.contains("/")) {
            // 提取最后一个斜杠后的方法名
            String[] pathParts = rawPath.split("/");
            methodName = pathParts[pathParts.length - 1];              // ->  create
        }

        // 获取原来的 summary（来自 @ApiOperation 的 value）
        String originalSummary = context.operationBuilder().build().getSummary();

        // 拼接格式：方法名 + 原中文注释
        String finalSummary = methodName;
        if (originalSummary != null && !originalSummary.trim().isEmpty()) {
            finalSummary = methodName + " " + originalSummary.trim();
        }

        context.operationBuilder().summary(finalSummary);

        // 2) operationId：可读且唯一
        String opId = methodName.replaceAll("[^a-zA-Z0-9_]", "_");
        context.operationBuilder().uniqueId(opId);
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        // 兼容 Swagger2 与 OpenAPI3（springfox 3.x）
        return SWAGGER_2.equals(delimiter) || OAS_30.equals(delimiter);
    }
}
