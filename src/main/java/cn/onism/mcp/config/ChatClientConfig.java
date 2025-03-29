package cn.onism.mcp.config;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
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

    @Resource
    private OpenAiChatModel model;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(model)
                // 默认加载所有的工具，避免重复 new
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

}
