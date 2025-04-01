package cn.onism.mcp.config;

import jakarta.annotation.Resource;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * rag 嵌入配置
 *
 * @author Onism
 * @date 2025-03-30
 */
@Configuration
public class RagEmbeddingConfig {

    @Resource
    private OllamaApi ollamaApi;

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    /**
     * pg 向量库存储
     *
     * @param jdbcTemplate JDBC 模板
     * @return {@link PgVectorStore }
     */
    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate) {
        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                // 设置向量模型
                .defaultOptions(OllamaOptions.builder().model("nomic-embed-text")
                        .numBatch(1024).build())
                .build();
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                // 设置表名
                .vectorTableName("vector_knowledge")
                // 校验表是否存在
                .vectorTableValidationsEnabled(true)
                // 默认是 768
                .dimensions(768)
                .build();
    }
}
