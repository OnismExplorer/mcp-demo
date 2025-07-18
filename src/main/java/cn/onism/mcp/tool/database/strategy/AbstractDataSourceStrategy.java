package cn.onism.mcp.tool.database.strategy;

import cn.onism.mcp.tool.database.strategy.config.DataSourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;


/**
 * 抽象数据库策略
 *
 * @author Onism
 * @date 2025-03-24
 */
public abstract class AbstractDataSourceStrategy implements DataSourceStrategy {

    /**
      * 后续有其他类型的数据源(如 Oracle、sqlite 等)，直接补充常量即可(不建议在代码中硬编码魔法值)
      */
    protected static final String MYSQL = "mysql";
    protected static final String ORACLE = "oracle";
    protected static final String POSTGRESQL = "postgres";

    protected HikariDataSource dataSource;
    /**
     * 允许关键字
     */
    private static final Set<String> ALLOWED_KEYWORDS = Set.of(
            // 基础查询
            "SELECT", "*", "FROM", "AS",
            // 表连接
            "JOIN", "INNER", "LEFT", "RIGHT", "OUTER", "ON",
            // 条件过滤
            "WHERE", "AND", "OR", "NOT",
            "BETWEEN", "IN", "LIKE", "IS", "NULL", "EXISTS",
            // 分组与排序
            "GROUP", "ORDER", "BY", "ASC", "DESC", "HAVING",
            // 分页与限制
            "LIMIT", "OFFSET",
            // 集合操作
            "UNION", "ALL",
            // 高级查询
            "DISTINCT", "CASE", "WHEN", "THEN", "ELSE", "END",
            "WITH", "EXPLAIN"
    );

    /**
     * 初始化数据源参数
     *
     * @param config 配置
     */
    @Override
    public void init(DataSourceProperties.DataSourceProperty config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeOut());

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * 关闭数据源
     */
    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            return processResultSet(stmt.executeQuery());
        }
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql, Map<Integer, Object> params) throws SQLException {
        validate(sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParameters(stmt,params);
            return processResultSet(stmt.executeQuery());
        }
    }

    /**
     * 绑定参数
     *
     * @param statement 语句
     * @param params    参数列表
     * @throws SQLException SQL 异常
     */
    protected void bindParameters(PreparedStatement statement,Map<Integer, Object> params)
            throws SQLException {

        for (Map.Entry<Integer, Object> entry : params.entrySet()) {
            int index = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Temporal) { // 处理Java 8+时间类型
                if (value instanceof LocalDate v) {
                    statement.setDate(index, Date.valueOf(v));
                } else if (value instanceof LocalDateTime v) {
                    statement.setTimestamp(index, Timestamp.valueOf(v));
                }
            } else {
                statement.setObject(index, value); // 通用处理
            }
        }
    }

    /**
     * 处理返回结果集
     * @param rs 结果集
     * @return {@link List }<{@link Map }<{@link String }, {@link Object }>>
     * @throws SQLException sql异常
     */
    protected List<Map<String, Object>> processResultSet(ResultSet rs)
            throws SQLException {

        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i).toLowerCase();
                Object value = rs.getObject(i);

                // 转换SQL日期到Java时间
                if (value instanceof Date) {
                    value = ((Date) value).toLocalDate();
                }
                row.put(columnName, value);
            }
            resultList.add(row);
        }
        return resultList;
    }

    /**
     * 校验 SQL 语句(这里只是简单校验了一下)
     * @param sql SQL
     */
    protected void validate(String sql) {
        String[] tokens = sql.split("\\s+");
        for (String token : tokens) {
            if (!ALLOWED_KEYWORDS.contains(token.toUpperCase())) {
                // 这里只允许查询语句通过
                // 例如：drop table 等危险操作会被拦截(避免人为恶意删库跑路(bushi))
                throw new SecurityException("禁止的SQL操作: " + token);
            }
        }
    }

}
