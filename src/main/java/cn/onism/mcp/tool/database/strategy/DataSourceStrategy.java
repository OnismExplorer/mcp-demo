package cn.onism.mcp.tool.database.strategy;

import cn.onism.mcp.tool.database.strategy.config.DataSourceProperties;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库策略
 *
 * @author Onism
 * @date 2025-03-24
 */
public interface DataSourceStrategy {

    /**
     * 根据配置初始化数据源
     */
    void init(DataSourceProperties.DataSourceProperty config);

    /**
     * 获取数据库类型（代替之前的类型推断）
     */
    String getDbType();

    /**
     * 执行查询(无需封装参数)
     *
     * @param sql SQL
     * @return {@link List }<{@link Map }<{@link String },{@link Object }>>
     * @throws SQLException sql异常
     */
    List<Map<String,Object>> executeQuery(@NotNull String sql) throws SQLException;

    /**
     * 执行查询(需要手动封装参数)
     */
    List<Map<String, Object>> executeQuery(@NotNull String sql, Map<Integer, Object> params) throws SQLException;

    /**
     * 关闭连接池
     */
    void shutdown();
}
