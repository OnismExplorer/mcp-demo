package cn.onism.mcp.tool.database;

import cn.onism.mcp.annotations.McpTool;
import cn.onism.mcp.tool.database.manage.DataSourceManager;
import cn.onism.mcp.tool.database.strategy.DataSourceStrategy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库工具
 *
 * @author Onism
 * @date 2025-03-24
 */
@Component
@McpTool
public class DatabaseTool {

    /**
     * 数据源管理器
     */
    private final DataSourceManager dataSourceManager;

    @Autowired
    public DatabaseTool(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }


    @Tool(description = "执行数据库语句，其中 datasourceId 为数据源；param 为需要封装的参数，key 为封装参数的索引位置，value 为封装参数的值")
    public DatabaseResponse executeSQL(DatabaseRequest request) {
        try {
            DataSourceStrategy strategy = dataSourceManager.getStrategy(request.getDatasourceId());
            List<Map<String, Object>> result = strategy.executeQuery(
                    request.getSql()
            );
            return new DatabaseResponse(result, null);
        } catch (SQLException e) {
            return new DatabaseResponse(null, "SQL执行错误: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return new DatabaseResponse(null, e.getMessage());
        }
    }

    @Setter
    @Getter
    public static class DatabaseRequest {
        /**
         * 数据源唯一标识
         */
        @NotNull
        private String datasourceId;

        /**
         * SQL 语句
         */
        @NotNull
        private String sql;

        private Map<Integer, Object> params;

    }

    @Setter
    @Getter
    public static class DatabaseResponse {
        private List<Map<String, Object>> data;
        private String error;

        public DatabaseResponse(List<Map<String, Object>> data, String error) {
            this.data = data;
            this.error = error;
        }

    }
}
