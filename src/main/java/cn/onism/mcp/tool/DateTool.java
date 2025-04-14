package cn.onism.mcp.tool;

import cn.onism.mcp.annotations.McpTool;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具
 *
 * @author Onism
 * @date 2025-03-24
 */
@Component
@McpTool
public class DateTool {

    /**
     * 地址请求
     *
     * @author Onism
     * @date 2025-03-28
     */
    @Setter
    @Getter
    public static class AddressRequest {
        private String address;

    }

    /**
     * 日期响应
     *
     * @author Onism
     * @date 2025-03-28
     */
    @Setter
    @Getter
    public static class DateResponse {
        private String result;

        public DateResponse(String result) {
            this.result = result;
        }

    }

    @Tool(description = "获取指定地点的当前时间")
    public DateResponse getAddressDate(AddressRequest request) {
        String result = String.format("%s的当前时间是%s",
                request.getAddress(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return new DateResponse(result);
    }
}
