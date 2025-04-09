package cn.onism.mcp.config;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天客户端配置
 *
 * @author Onism
 * @date 2025-03-25
 */
@Configuration
public class ChatClientConfig {

    /**
     * OpenAI 聊天模型
     */
    @Resource
    private OpenAiChatModel openAiChatModel;

    /**
     * OLLAMA 聊天模型
     */
    @Resource
    private OllamaChatModel ollamaChatModel;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * OpenAI 聊天客户端
     *
     * @return {@link ChatClient }
     */
    @Bean(name = "openAiChatClient")
    public ChatClient openAiChatClient() {
        return ChatClient.builder(openAiChatModel)
                // 默认加载所有的工具，避免重复 new
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    /**
     * OLLAMA 聊天客户端
     *
     * @return {@link ChatClient }
     */
    @Bean(name = "ollamaChatClient")
    public ChatClient ollamaChatClient() {
        return ChatClient.builder(ollamaChatModel)
                // 默认加载所有的工具，避免重复 new
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

}
