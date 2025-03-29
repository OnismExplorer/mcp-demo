package cn.onism.mcp;

import cn.onism.mcp.tool.EmailTool;
import cn.onism.mcp.tool.MonitorTool;
import cn.onism.mcp.tool.database.DatabaseTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class McpDemoApplicationTests {

    @Resource
    private DatabaseTool databaseTool;

    @Resource
    private MonitorTool monitorTool;

    @Resource
    private EmailTool emailTool;


    /**
     * 测试数据库
     */
    @Test
    void testDatabase() {
        DatabaseTool.DatabaseRequest queryRequest = new DatabaseTool.DatabaseRequest();
        // 先查询 postgres 数据库中数据
        queryRequest.setDatasourceId("postgres");
        queryRequest.setSql("select * from friends where id = 1");
        DatabaseTool.DatabaseResponse queryResult = databaseTool.executeSQL(queryRequest);
        StringBuilder result = new StringBuilder(queryResult.getData().toString());

        // 再查询 mysql 数据库中数据
        queryRequest.setDatasourceId("mysql");
        queryRequest.setSql("select * from friends where id = 2");
        queryResult = databaseTool.executeSQL(queryRequest);
        result.append(queryResult.getData().toString());

        System.out.println(result);
    }

    /**
     * 测试系统监控功能
     */
    @Test
    void testMonitor() {
        MonitorTool.MonitorResponse response = monitorTool.getResourceUsage(new MonitorTool.MonitorRequest());
        String result = "系统资源使用率：" + "\n" +
                response.getMetrics().toString() + "\n" + "\n" +
                "系统资源详情信息：" + "\n" +
                MonitorTool.getMemoryInfo() + "\n" +
                MonitorTool.getThreadInfo() + "\n" +
                MonitorTool.getCpuInfo() + "\n";

        System.out.println(result);
    }

    /**
     * 测试发送邮件功能
     */
    @Test
    void testSendEmail() {
        EmailTool.EmailRequest emailRequest = new EmailTool.EmailRequest();
        emailRequest.setEmail("xxxxxxx@qq.com");
        emailRequest.setSubject("生日快乐祝福");
        emailRequest.setMessage("生日快乐！愿你的每一天都充满阳光和快乐！");
        emailTool.sendMailMessage(emailRequest);
    }
}
