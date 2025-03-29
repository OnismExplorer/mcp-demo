package cn.onism.mcp.tool.database.strategy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 数据源属性
 *
 * @author Onism
 * @date 2025-03-27
 */
@ConfigurationProperties(prefix = "spring.ai.datasources", ignoreInvalidFields = true)
public class DataSourceProperties {

    @Valid
    private List<DataSourceProperty> datasource;

    public List<DataSourceProperty> getDatasource() {
        return datasource;
    }

    public void setDatasource(List<DataSourceProperty> datasource) {
        this.datasource = datasource;
    }

    public static class DataSourceProperty {

        /**
         * 数据源唯一标识
         */
        @NotNull
        private String id;
        /**
         * 数据源 url
         */
        @NotNull
        private String url;
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
        /**
         * 数据源类型
         */
        @Pattern(regexp = "(?i)mysql|postgresql|oracle", message = "不支持的数据库类型")
        private String type;
        /**
         * 驱动程序类名称
         */
        private String driverClassName;

        /**
         * 连接池大小
         */
        private int maxPoolSize = 10;
        /**
         * 连接超时时长
         */
        private int connectionTimeOut = 30000;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getConnectionTimeOut() {
            return connectionTimeOut;
        }

        public void setConnectionTimeOut(int connectionTimeOut) {
            this.connectionTimeOut = connectionTimeOut;
        }
    }
}
