package cn.onism.mcp.tool.database.manage;

import cn.onism.mcp.tool.database.strategy.DataSourceStrategy;
import cn.onism.mcp.tool.database.strategy.config.DataSourceProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据源管理器
 *
 * @author Onism
 * @date 2025-03-25
 */
@Component
public class DataSourceManager {
    /**
     * 策略映射
     */
    private final Map<String, DataSourceStrategy> strategyMap = new ConcurrentHashMap<>();
    /**
     * 数据源参数
     */
    private final DataSourceProperties properties;
    /**
     * 策略实施
     */
    private final Map<String, DataSourceStrategy> strategyImplementations;

    @Autowired
    public DataSourceManager(DataSourceProperties properties,
                             List<DataSourceStrategy> strategies) {
        this.properties = properties;
        this.strategyImplementations = strategies.stream()
                .collect(Collectors.toMap(DataSourceStrategy::getDbType, Function.identity()));

        initDataSources();
    }

    /**
     * 初始化数据源
     */
    private void initDataSources() {
        for (DataSourceProperties.DataSourceProperty config : properties.getDatasource()) {
            DataSourceStrategy strategy = strategyImplementations.get(config.getType().toLowerCase());
            if (strategy != null) {
                strategy.init(config);
                strategyMap.put(config.getId(), strategy);
            }
        }
    }

    /**
     * 根据数据源 ID(唯一标识) 获取数据源策略
     *
     * @param datasourceId 数据源 ID
     * @return {@link DataSourceStrategy }
     */
    public DataSourceStrategy getStrategy(String datasourceId) {
        DataSourceStrategy strategy = strategyMap.get(datasourceId);
        if (strategy == null) {
            throw new IllegalArgumentException("未配置的数据源 ID: " + datasourceId);
        }
        return strategy;
    }

    /**
     * 根据数据源类型获取数据源策略
     *
     * @param datasourceType 数据源类型
     * @return {@link List }<{@link DataSourceStrategy }>
     */
    public List<DataSourceStrategy> getStrategys(String datasourceType) {
        // 根据 数据源类型 分组
        Map<String, List<DataSourceStrategy>> strategysMap = this.strategyMap.values()
                // 使用并行流的方式进行处理
                .parallelStream()
                .collect(Collectors.groupingBy(DataSourceStrategy::getDbType));

        return strategysMap.getOrDefault(datasourceType,new ArrayList<>());
    }

    @PreDestroy
    public void destroy() {
        strategyMap.values().forEach(DataSourceStrategy::shutdown);
    }
}
