package cn.onism.mcp.tool.database.strategy;

import org.springframework.stereotype.Component;

/**
 * MySQL 数据源策略
 *
 * @author Onism
 * @date 2025-03-24
 */
@Component
public class MySQLStrategy extends AbstractDataSourceStrategy {
    @Override
    public String getDbType() {
        return MYSQL;
    }
}
