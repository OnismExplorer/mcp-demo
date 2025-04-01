package cn.onism.mcp.service;

import cn.onism.mcp.constants.CodeEnum;
import cn.onism.mcp.entity.VectorRelation;
import cn.onism.mcp.exception.CustomException;
import cn.onism.mcp.repository.VectorRelationRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vector 关系服务
 *
 * @author Onism
 * @date 2025-04-01
 */
@Service
public class VectorRelationService {

    @Resource
    private VectorRelationRepository repository;

    /**
     * 创建记录
     *
     * @param relation 关系
     * @return {@link VectorRelation }
     */
    @Transactional
    public VectorRelation createRelation(VectorRelation relation) {
        return repository.save(relation);
    }

    public VectorRelation getByFileHash(String fileHash) {
        return repository.getByFileHash(fileHash);
    }

    /**
     * 按 ID 获取
     *
     * @param id 身份证
     * @return {@link VectorRelation }
     */
    public VectorRelation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomException(CodeEnum.DATA_NOT_EXIST));
    }

    @Transactional
    public void updateById(VectorRelation relation) {
        getById(relation.getId());
        repository.save(relation);
    }
}
