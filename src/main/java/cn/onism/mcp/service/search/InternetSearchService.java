package cn.onism.mcp.service.search;

import cn.onism.mcp.entity.SearchResult;
import cn.onism.mcp.util.prompt.InternetSearchPromptBuilder;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 联网搜索服务
 *
 * @author Onism
 * @date 2025-04-02
 */
@Service
public class InternetSearchService {

    @Resource
    private SearxngSearchService searxngSearchService;

    /**
     * 聊天客户端
     */
    @Resource
    private ChatClient chatClient;


    /**
     * SearXNG 联网搜索
     *
     * @param question 问题
     * @return {@link String }
     */
    public String searXNGSearch(String question) {
        // 执行搜索
        List<SearchResult> results = searxngSearchService.search(question);

        // 构建增强Prompt
        String augmentedPrompt = InternetSearchPromptBuilder.buildRAGPrompt(question, results);

        // 调用大模型
        return chatClient.prompt(new Prompt(augmentedPrompt)).call().content();
    }

    /**
     * searXNG 联网搜索(流式)
     *
     * @param question 问题
     * @return {@link Flux }<{@link ChatResponse }>
     */
    public Flux<ChatResponse> searXNGstreamSearch(String question) {
        // 执行搜索
        List<SearchResult> results = searxngSearchService.search(question);

        // 构建增强Prompt
        String augmentedPrompt = InternetSearchPromptBuilder.buildRAGPrompt(question, results);

        // 调用大模型
        return chatClient.prompt(new Prompt(augmentedPrompt)).stream().chatResponse();
    }
}
