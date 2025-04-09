package cn.onism.mcp.tool;

import cn.hutool.json.JSONUtil;
import cn.hutool.system.oshi.OshiUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.io.File;
import java.lang.management.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 监控工具
 *
 * @author Onism
 * @date 2025-03-25
 */
@Slf4j
@Component
public class MonitorTool {

    private enum MonitorResponseType {
        SUCCESS, FAIL
    }

    /**
     * HTTP 客户端
     */
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 存储监控配置
     */
    private static final Map<String, MonitoringConfig> monitoringConfigs = new ConcurrentHashMap<>();

    /**
     * 已启用监控
     */
    @Value("${spring.ai.monitor.enabled}")
    private static boolean MONITORING_ENABLED;

    /**
     * 监控线程
     */
    private static Thread monitoringThread;

    /**
     * Webhook 回调URL
     */
    @Value("${spring.ai.monitor.web-hook-url}")
    private static String webhookUrl;

    /**
     * 阈值
     */
    @Value("${spring.ai.monitor.threshold:0.75}")
    private static double threshold;

    /**
     * 检查间隔(单位：秒)，默认值 5 (秒)
     */
    @Value("${spring.ai.monitor.check-interval:5}")
    private static int checkInterval;

    /**
     * OSHI 获取系统信息
     */
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();

    @Tool(description = "获取系统资源使用率")
    public MonitorResponse getResourceUsage(MonitorRequest request) {
        Map<String, Object> metrics = new HashMap<>();
        try {
            if (request.getResourceType() == null || request.getResourceType() == ResourceType.CPU) {
                metrics.put("cpuUsage", getCpuUsage());
            }
            if (request.getResourceType() == null || request.getResourceType() == ResourceType.MEMORY) {
                metrics.put("memoryUsage", getMemoryUsage());
            }
            if (request.getResourceType() == null || request.getResourceType() == ResourceType.DISK) {
                metrics.put("diskUsage", getResourceUsage(ResourceType.DISK));
            }

            if (request.getResourceType() == null || request.getResourceType() == ResourceType.THREADS) {
                metrics.put("threads", getResourceUsage(ResourceType.THREADS));
            }
            return new MonitorResponse(MonitorResponseType.SUCCESS.name(), "资源状态获取成功", metrics);
        } catch (Exception e) {
            log.error("获取系统资源使用率，失败原因：{}",e.getMessage());
            return new MonitorResponse(MonitorResponseType.FAIL.name(), "监控数据获取失败: " + e.getMessage(), null);
        }
    }

    @Tool(description = "获取系统资源详情信息，resourceType 为资源类型，传 null 则表明获取所有资源类型(CPU、内存、线程、磁盘等)")
    public MonitorResponse getResourceDetail(MonitorRequest request) {
        Map<String, Object> metrics = new HashMap<>();
        try {
            if (request.getResourceType() == null || request.getResourceType() == ResourceType.CPU) {
                metrics.put("cpuDetail", getCpuInfo());
            }
            if (request.getResourceType() == null || request.getResourceType() == ResourceType.MEMORY) {
                metrics.put("memoryDetail", getMemoryInfo());
            }

            if (request.getResourceType() == null || request.getResourceType() == ResourceType.THREADS) {
                metrics.put("threadDetail", getThreadInfo());
            }
            if (request.getResourceType() == null || request.getResourceType() == ResourceType.DISK) {
                metrics.put("dickUsage", getResourceUsage(ResourceType.DISK));
            }
            return new MonitorResponse(MonitorResponseType.SUCCESS.name(), "资源详情获取成功", metrics);
        } catch (Exception e) {
            log.error("获取系统资源详情数据失败，失败原因：{}",e.getMessage());
            return new MonitorResponse(MonitorResponseType.FAIL.name(), "资源详情数据获取失败: " + e.getMessage(), null);
        }
    }

    @Tool(description = "设置资源监控参数并启用主动监控")
    public MonitorResponse setupMonitoring(MonitorRequest request) {
        try {
            MonitoringConfig config = new MonitoringConfig();
            config.setResourceType(request.getResourceType());
            config.setThreshold(threshold);
            if (StringUtils.isNotBlank(webhookUrl)) {
                config.setWebhookUrl(webhookUrl);
            }
            config.setCheckIntervalSeconds(checkInterval);

            monitoringConfigs.put(request.getResourceType().name(), config);

            if (MONITORING_ENABLED) {
                startMonitoringThread();
            }

            return new MonitorResponse(MonitorResponseType.SUCCESS.name(), "监控配置已更新并启用", null);
        } catch (Exception e) {
            log.error("监控配置失败，失败原因：{}",e.getMessage());
            return new MonitorResponse(MonitorResponseType.FAIL.name(), "监控配置失败: " + e.getMessage(), null);
        }
    }

    /**
     * 开始监控线程
     */
    private static void startMonitoringThread() {
        if (monitoringThread == null || !monitoringThread.isAlive()) {
            monitoringThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    monitoringConfigs.values().forEach(config -> {
                        double currentUsage = getResourceUsage(config.getResourceType());
                        if (currentUsage > config.getThreshold()) {
                            triggerAlert(config, currentUsage);
                        }
                        try {
                            Thread.sleep(config.getCheckIntervalSeconds() * 1000L);
                        } catch (InterruptedException e) {
                            log.error("开启监控线程失败！");
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            });
            monitoringThread.setDaemon(true);
            monitoringThread.start();
        }
    }

    /**
     * @param type 资源类型
     * @return double
     */
    private static double getResourceUsage(ResourceType type) {
        return switch (type) {
            case CPU -> getCpuUsage();
            case MEMORY -> getMemoryUsage();
            case DISK -> getDiskUsage(null);
            case THREADS -> {
                ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
                yield (double) threadBean.getThreadCount() / threadBean.getPeakThreadCount() * 100;
            }
        };
    }

    /**
     * 获取 CPU 使用率
     *
     * @return double
     */
    private static double getCpuUsage() {
        return Double.parseDouble(java.lang.String.format("%.2f", 100 - OshiUtil.getCpuInfo().getFree()));
    }

    /**
     * 获取 CPU 信息
     *
     * @return {@link String }
     */
    public static String getCpuInfo() {
        return getCpuInfo(SYSTEM_INFO.getHardware().getProcessor());
    }

    private static String getCpuInfo(CentralProcessor processor) {
        long[] preTicks = processor.getSystemCpuLoadTicks(); // 保存第一次ticks

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "CPU信息获取失败";
        }

        long[] postTicks = processor.getSystemCpuLoadTicks();
        Map<CentralProcessor.TickType, Long> tickDiffs = calculateTickDiffs(preTicks, postTicks);

        long total = tickDiffs.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return "无法计算CPU使用率";

        return buildCpuInfoString(processor, tickDiffs, total, preTicks); // 传递preTicks
    }


    /**
     * 动态计算 ticks 差值
     *
     * @param preTicks  前 ticks 值
     * @param postTicks 后 ticks 值
     * @return {@link Map }<{@link CentralProcessor.TickType }, {@link Long }>
     */
    private static Map<CentralProcessor.TickType, Long> calculateTickDiffs(long[] preTicks, long[] postTicks) {
        Map<CentralProcessor.TickType, Long> diffs = new EnumMap<>(CentralProcessor.TickType.class);
        for (CentralProcessor.TickType type : CentralProcessor.TickType.values()) {
            int idx = type.getIndex();
            diffs.put(type, postTicks[idx] - preTicks[idx]);
        }
        return diffs;
    }


    /**
     * 构建 CPU 信息结果字符串
     *
     * @param processor 中央处理器(CPU)
     * @param diffs     差异
     * @param total     总
     * @param preTicks  前报价
     * @return {@link String }
     */
    private static String buildCpuInfoString(CentralProcessor processor,
                                             Map<CentralProcessor.TickType, Long> diffs,
                                             long total,
                                             long[] preTicks) {
        double systemUsage = percentage(diffs.get(CentralProcessor.TickType.SYSTEM), total);
        double userUsage = percentage(diffs.get(CentralProcessor.TickType.USER), total);
        double ioWait = percentage(diffs.get(CentralProcessor.TickType.IOWAIT), total);
        double idle = percentage(diffs.get(CentralProcessor.TickType.IDLE), total);

        return java.lang.String.format(
                """
                        CPU 核数: %d
                        系统使用率: %.2f%%
                        用户使用率: %.2f%%
                        I/O 等待率: %.2f%%
                        空闲率: %.2f%%
                        Tick负载: %.1f%%
                        OS上报负载: %.1f%%""",
                processor.getLogicalProcessorCount(),
                systemUsage,
                userUsage,
                ioWait,
                idle,
                processor.getSystemCpuLoadBetweenTicks(preTicks) * 100, // 传入preTicks
                processor.getSystemCpuLoad(1000) * 100
        );
    }

    /**
     * 计算百分比
     *
     * @param part  部分
     * @param total 总
     * @return double
     */
    private static double percentage(long part, long total) {
        return total == 0 ? 0.0 : (part * 100.0) / total;
    }

    /**
     * 获取内存使用情况
     *
     * @return double
     */
    private static double getMemoryUsage() {
        double usage = (double) (OshiUtil.getMemory().getTotal() - OshiUtil.getMemory()
                .getAvailable()) / OshiUtil.getMemory().getTotal() * 100;
        return Double.parseDouble(java.lang.String.format("%.2f", usage));
    }

    /**
     * 获取内存信息
     *
     * @return {@link String }
     */
    public static String getMemoryInfo() {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();

        StringBuilder builder = new StringBuilder();

        // 堆内存信息
        builder.append("堆内存信息：").append(getMemoryInfo(memoryPoolMXBeans, MemoryType.HEAP)).append("\n");

        // 非堆内存信息
        builder.append("非堆内存信息：").append(getMemoryInfo(memoryPoolMXBeans, MemoryType.NON_HEAP)).append("\n");

        // NIO 相关内存
        try {
            Class clazz = Class.forName("java.lang.management.BufferPoolMXBean");
            List<BufferPoolMXBean> bufferPoolMXBeans = ManagementFactory.getPlatformMXBeans(clazz);

            builder.append("NIO 相关内容：");
            bufferPoolMXBeans.forEach(x -> builder.append("name: ")
                    .append(x.getName())
                    // 已使用的内存
                    .append(" 已使用内存: ")
                    .append(x.getMemoryUsed() / 1024 / 1024).append(" MB") // 以 MB 为单位
                    // 已申请的内存
                    .append(" 容量: ")
                    .append(x.getTotalCapacity() / 1024 / 1024).append(" MB").append("\n"));
        } catch (ClassNotFoundException ignore) {

        }

        return builder.toString();
    }

    /**
     * 获取内存信息
     *
     * @param memoryPoolMXBeans 内存池 MXBebeans
     * @param type              类型
     * @return {@link String }
     */
    private static String getMemoryInfo(List<MemoryPoolMXBean> memoryPoolMXBeans, MemoryType type) {
        StringBuilder builder = new StringBuilder();
        memoryPoolMXBeans.parallelStream().filter(x -> x.getType().equals(type))
                .forEach(x -> {
                    String info = "名称: " +
                            x.getName() +
                            // 已使用的内存
                            " 已使用内存: " +
                            x.getUsage().getUsed() / 1024 / 1024 + " MB" + // 以 MB 为单位
                            // 已申请的内存
                            " 已申请内存: " +
                            x.getUsage().getCommitted() / 1024 / 1024 + " MB" +
                            // 最大的内存
                            " 能申请最大内存: " +
                            x.getUsage().getMax() / 1024 / 1024 + " MB";
                    builder.append(info).append("\n");
                });

        return builder.toString();
    }

    /**
     * 获取磁盘使用情况(默认为系统盘：C盘 or /)
     *
     * @return double
     */
    private static double getDiskUsage(String diskPath) {
        if (diskPath == null) {
            return getDiskUsage(getDefaultDiskPath());
        }

        File file = new File(diskPath);
        boolean isValid = checkPathValidity(file);

        if (!isValid) {
            // 路径无效时，回退到操作系统默认磁盘
            String defaultPath = getDefaultDiskPath();
            if (!diskPath.equals(defaultPath)) { // 避免重复调用
                return getDiskUsage(defaultPath); // 递归查询默认磁盘
            } else {
                return 0.0; // 默认路径也无效时终止递归
            }
        }

        try {
            long total = file.getTotalSpace();
            if (total == 0) return 0.0;
            long used = total - file.getFreeSpace();
            return Double.parseDouble(String.format("%.2f",(double) used / total * 100));
        } catch (SecurityException e) {
            System.err.println("权限不足，无法访问磁盘: " + diskPath);
            return 0.0;
        }
    }

    /**
     * 校验路径是否有效（存在且有容量）
     */
    private static boolean checkPathValidity(File file) {
        try {
            return file.exists() && file.getTotalSpace() > 0;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * 获取操作系统默认磁盘路径
     */
    private static String getDefaultDiskPath() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "C:\\\\" : "/";
    }

    /**
     * 获取线程信息
     *
     * @return {@link String }
     */
    public static String getThreadInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        // 所有的线程信息
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(threadMXBean.isObjectMonitorUsageSupported(),
                threadMXBean.isSynchronizerUsageSupported());

        StringBuilder builder = new StringBuilder();
        for (ThreadInfo threadInfo : threadInfos) {
            builder.append("线程名称：").append(threadInfo.getThreadName()).append("\n")
                    .append("线程 ID：").append(threadInfo.getThreadId()).append("\n")
                    .append("线程状态：").append(threadInfo.getThreadState()).append("\n");

            builder.append("堆信息：");
            for (StackTraceElement traceElement : threadInfo.getStackTrace()) {
                builder.append(JSONUtil.toJsonStr(traceElement)).append("\n");
            }
        }

        return builder.toString();
    }

    // 告警触发方法
    private static void triggerAlert(MonitoringConfig config, double currentUsage) {
        String message = java.lang.String.format("[系统告警] %s使用率过高: %.2f%% > 阈值 %.2f%%",
                config.getResourceType(), currentUsage, config.getThreshold());

        if (config.getWebhookUrl() != null) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.getWebhookUrl()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                java.lang.String.format("{\"alert\": \"%s\", \"timestamp\": %d}",
                                        message, System.currentTimeMillis())))
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> log.info("告警通知发送成功: {}", response.statusCode()))
                        .exceptionally(e -> {
                            log.error("告警通知发送失败", e);
                            return null;
                        });
            } catch (Exception e) {
                log.error("创建HTTP请求失败", e);
            }
        }
    }

    /**
     * 资源类型
     *
     * @author Onism
     * @date 2025-03-25
     */
    public enum ResourceType {
        CPU, MEMORY, DISK, THREADS
    }

    /**
     * 监控配置
     *
     * @author Onism
     * @date 2025-03-25
     */
    @Setter
    @Getter
    public static class MonitoringConfig {
        private ResourceType resourceType;
        private double threshold;
        private String webhookUrl;
        private int checkIntervalSeconds = 5;

    }

    /**
     * 监控请求
     *
     * @author Onism
     * @date 2025-03-25
     */
    @Setter
    @Getter
    public static class MonitorRequest {
        /**
         * 资源类型
         */
        @ToolParam(required = false, description = "需要获取的系统资源类型，传 null 则表明获取所有资源类型(CPU、内存、线程、磁盘等)")
        private ResourceType resourceType;
    }

    /**
     * 监控响应
     *
     * @author Onism
     * @date 2025-03-25
     */
    @Getter
    @Setter
    public static class MonitorResponse {
        /**
         * 状态
         */
        private String status;
        /**
         * 消息
         */
        private String message;
        /**
         * 指标
         */
        private Map<String, Object> metrics;

        public MonitorResponse(String status, String message, Map<String, Object> metrics) {
            this.status = status;
            this.message = message;
            this.metrics = metrics;
        }

    }
}
