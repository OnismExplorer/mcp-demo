package cn.onism.mcp;

import cn.onism.mcp.tool.database.strategy.config.DataSourceProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MCP 客户端应用程序
 *
 * @author Onism
 * @date 2025-03-27
 */
@SpringBootApplication
@EnableConfigurationProperties(DataSourceProperties.class)
public class McpDemoApplication {

    public static void main(String[] args) {
        // 加载.env文件
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // 将变量设置为系统属性
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        SpringApplication.run(McpDemoApplication.class, args);
    }
}
