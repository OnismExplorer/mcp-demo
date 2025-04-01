package cn.onism.mcp.service;

import cn.onism.mcp.entity.VectorRelation;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文档服务
 *
 * @author Onism
 * @date 2025-03-30
 */
@Service
public class DocumentService {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private VectorRelationService vectorRelationService;

    /**
     * 处理文档
     *
     * @param file 文件
     * @throws IOException io异常
     */
    public void processDocument(MultipartFile file) throws IOException {
        // 计算文件 hash
        String fileHash = generateFileHash(file);

        // 查询是否已经存在该文件
        VectorRelation vectorRelation = vectorRelationService.getByFileHash(fileHash);

        if(vectorRelation != null){
            // 更新记录
            vectorRelationService.updateById(vectorRelation);
        } else { // 新增操作
            // 解析文档
            TikaDocumentReader tikaReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = tikaReader.read();

            // 分割文本
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> documentList = splitter.transform(documents);
            // 添加文件名称分类
            documentList.forEach(doc -> doc.getMetadata().put("fileName",file.getOriginalFilename()));

            // 存储到表
            vectorStore.accept(documentList);

            // 插入新记录
            vectorRelation = new VectorRelation()
                    .setFileName(file.getOriginalFilename())
                    .setFileHash(fileHash);

            vectorRelationService.createRelation(vectorRelation);
        }
    }

    /**
     * 生成文件哈希值
     *
     * @param file 文件
     * @return {@link String }
     */
    private String generateFileHash(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.sha256Hex(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
