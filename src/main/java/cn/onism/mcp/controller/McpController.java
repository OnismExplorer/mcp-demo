package cn.onism.mcp.controller;

import cn.onism.mcp.common.Result;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * MCP 控制器
 *
 * @author Onism
 * @date 2025-03-26
 */
@RestController
public class McpController {

    @Resource
    private ChatClient openAiChatClient;

    /**
     * 提供一个对外的聊天接口
     *
     * @param message 消息
     * @return {@link Flux }<{@link String }>
     */
    @GetMapping("/chat")
    public Result<String> chat(
            @RequestParam String message,
            @RequestParam(defaultValue = "你是一个助手,请用中文回答。", required = false) String promptMessage
    ) {
        SystemMessage systemMessage = new SystemMessage(promptMessage);
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        String response = openAiChatClient.prompt(prompt)
                .call().content();

        return Result.success(response);
    }

    /**
     * 提供一个对外的聊天接口(流式)
     *
     * @param message 消息
     * @return {@link Flux }<{@link String }>
     */
    @GetMapping("/stream/chat")
    public Flux<ChatResponse> chatStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "你是一个助手,请用中文回答。", required = false) String promptMessage
    ) {
        SystemMessage systemMessage = new SystemMessage(promptMessage);
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return openAiChatClient.prompt(prompt)
                .stream().chatResponse();
    }
}
