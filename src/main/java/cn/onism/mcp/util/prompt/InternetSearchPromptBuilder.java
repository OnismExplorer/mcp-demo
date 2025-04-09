package cn.onism.mcp.util.prompt;

import cn.onism.mcp.entity.SearchResult;

import java.util.List;

/**
 * 提示词生成器
 *
 * @author Onism
 * @date 2025-04-08
 */
public class InternetSearchPromptBuilder {
    public static String buildRAGPrompt(String question, List<SearchResult> results) {
        StringBuilder context = new StringBuilder();
        context.append("基于以下联网搜索返回的结果，请生成专业回答：\n");

        results.forEach(result ->
                context.append(String.format("<context>\n[来源] %s\n[摘要] %s\n</context>\n",
                        result.getUrl(),
                        result.getContent())
                ));

        return String.format("%s\n问题：%s", context, question);
    }
}
