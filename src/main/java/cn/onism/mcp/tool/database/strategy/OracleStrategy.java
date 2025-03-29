package cn.onism.mcp.tool.database.strategy;

import org.springframework.stereotype.Component;

/**
 * Oracle 数据源策略
 *
 * @author Onism
 * @date 2025-03-27
 */
@Component
public class OracleStrategy extends AbstractDataSourceStrategy{
    @Override
    public String getDbType() {
        return ORACLE;
    }
}
