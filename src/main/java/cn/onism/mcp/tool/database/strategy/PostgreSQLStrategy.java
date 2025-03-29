package cn.onism.mcp.tool.database.strategy;

import org.springframework.stereotype.Component;

/**
 * Postgre SQL策略
 *
 * @author Onism
 * @date 2025-03-27
 */
@Component
public class PostgreSQLStrategy extends AbstractDataSourceStrategy {
    @Override
    public String getDbType() {
        return POSTGRESQL;
    }
}
