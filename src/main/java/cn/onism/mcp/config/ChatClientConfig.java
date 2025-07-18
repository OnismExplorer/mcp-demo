package cn.onism.mcp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 聊天客户端配置
 *
 * @author Onism
 * @date 2025-03-25
 */
@Configuration
public class ChatClientConfig {


    /**
     * 创建 OpenAI 聊天客户端 Bean。
     * <p>
     * 仅当 'spring.ai.chat.client.type' 属性值为 'openai' 时，此 Bean 才会被创建。
     *
     * @param openAiChatModel      由 Spring 自动注入的 OpenAI 模型
     * @param toolCallbackProvider 由 Spring 自动注入的工具回调提供者
     * @return {@link ChatClient}
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.ai.chat.client.type", havingValue = "openai")
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel, ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(openAiChatModel)
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    /**
     * 创建 OLLAMA 聊天客户端 Bean。
     * <p>
     * 当 'spring.ai.chat.client.type' 属性值为 'ollama'，或者该属性未设置时 (matchIfMissing = true)，
     * 此 Bean 会被创建。这使其成为默认选项。
     *
     * @param ollamaChatModel      由 Spring 自动注入的 Ollama 模型
     * @param toolCallbackProvider 由 Spring 自动注入的工具回调提供者
     * @return {@link ChatClient}
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.ai.chat.client.type", havingValue = "ollama", matchIfMissing = true)
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(ollamaChatModel)
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

}
