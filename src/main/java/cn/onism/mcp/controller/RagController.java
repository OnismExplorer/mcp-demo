package cn.onism.mcp.controller;

import cn.onism.mcp.common.Result;
import cn.onism.mcp.service.DocumentService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 控制器
 *
 * @author Onism
 * @date 2025-03-31
 */
@RestController
@RequestMapping("/rag")
public class RagController {
    @Resource
    private OllamaChatModel ollamaChatModel;

    @Resource
    private VectorStore vectorStore;

    @Resource
    private DocumentService documentService;

    private static final String PROMPT = """
                基于以下知识库内容回答问题：
                {context}
                问题：{question}
                """;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            documentService.processDocument(file);
        } catch (IOException e) {
            return Result.<String>fail().message(e.getMessage());
        }
        return Result.success();
    }

    @GetMapping("/inquire")
    public Result<String> inquire(@RequestParam String question) {
        //检索相似文档作为上下文
        List<Document> contextDocs = vectorStore.similaritySearch(question);

        // 构建提示词模板
        String context = null;
        if (contextDocs != null) {
            context = contextDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n"));
        }

        // 调用大模型回答问题
        return Result.success(ollamaChatModel.call(PROMPT.replace("{context}", context).replace("{question}", question)));
    }

    @GetMapping("/stream/inquire")
    public Flux<ChatResponse> streamInquire(
            @RequestParam String question
    ) {
        //检索相似文档作为上下文
        List<Document> contextDocs = vectorStore.similaritySearch(question);

        // 构建提示词模板
        String context = null;
        if (contextDocs != null) {
            context = contextDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n"));
        }

        // 设置提示词
        Prompt prompt = new Prompt(PROMPT.replace("{context}", context).replace("{question}", question));

        // 输出完成标识：["finishReason": "stop"]
        return ollamaChatModel.stream(prompt);
    }
}
