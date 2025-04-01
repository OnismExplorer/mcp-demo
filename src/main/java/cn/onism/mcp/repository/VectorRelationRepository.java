package cn.onism.mcp.repository;

import cn.onism.mcp.entity.VectorRelation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 向量关系存储库
 *
 * @author Onism
 * @date 2025-04-01
 */
public interface VectorRelationRepository  extends JpaRepository<VectorRelation, Long> {

    /**
     * 按文件哈希值获取
     *
     * @param fileHash 文件哈希
     * @return {@link VectorRelation }
     */
    VectorRelation getByFileHash(String fileHash);
}
