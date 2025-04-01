package cn.onism.mcp;

import cn.onism.mcp.entity.VectorRelation;
import cn.onism.mcp.service.DocumentService;
import cn.onism.mcp.service.VectorRelationService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RagEmbeddingTests {
    @Resource
    private DocumentService documentService;

    @Resource
    private VectorRelationService vectorRelationService;

    @Test
    public void testCreateVectorRelation() {
        VectorRelation vectorRelation = new VectorRelation().
                setFileName("introduce.txt")
                .setFileHash("11ioqokkaiqo");
        vectorRelationService.createRelation(vectorRelation);
    }
}
