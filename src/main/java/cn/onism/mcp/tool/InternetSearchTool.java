package cn.onism.mcp.tool;

import cn.hutool.json.JSONUtil;
import cn.onism.mcp.annotations.McpTool;
import cn.onism.mcp.service.search.SearxngSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 联网搜索工具
 *
 * @author Onism
 * @date 2025-04-08
 */
@Component
@Slf4j
@McpTool
public class InternetSearchTool {

    private final SearxngSearchService searxngSearchService;

    @Autowired
    public InternetSearchTool(SearxngSearchService searxngSearchService) {
        this.searxngSearchService = searxngSearchService;
    }

    @Tool(description = "实时联网搜索问题，返回搜索结果，需要整理处理后再返回")
    public String internetSearch(String question) {
        // 执行搜索
        return JSONUtil.toJsonStr(searxngSearchService.search(question));
    }

}
