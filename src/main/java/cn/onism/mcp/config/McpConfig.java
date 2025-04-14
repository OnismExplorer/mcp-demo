package cn.onism.mcp.config;

import cn.onism.mcp.annotations.McpTool;
import cn.onism.mcp.service.search.SearxngSearchService;
import cn.onism.mcp.tool.database.manage.DataSourceManager;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MCP 配置
 *
 * @author Onism
 * @date 2025-03-25
 */
@Configuration
public class McpConfig implements WebMvcConfigurer {

    @Resource
    private DataSourceManager manager;

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sendMailer;

    @Resource
    private SearxngSearchService searxngSearchService;

    @Resource
    private ApplicationContext context;

    /**
     * 添加工具回调提供程序
     *
     * @return {@link ToolCallbackProvider }
     */
    @Bean
    @Primary
    public ToolCallbackProvider addToolCallbackProvider() {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(
                        context.getBeansWithAnnotation(McpTool.class).values().toArray()
                )
                .build();
    }
}
