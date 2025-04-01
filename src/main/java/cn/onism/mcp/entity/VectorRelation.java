package cn.onism.mcp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 向量知识库关系实体类
 *
 * @author Onism
 * @date 2025-04-01
 */
@Entity
@Table(name ="vector_relation")
@Data
@Accessors(chain = true)
public class VectorRelation implements Serializable {
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文件 Hash 值
     */
    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Serial
    private static final long serialVersionUID = 1L;

    public VectorRelation() {
    }


}
