-- 创建数据库
CREATE DATABASE ai_knowledge
    -- 指定所有者（替换为你的用户名）
    OWNER = root
    -- 设置字符编码为 UTF8
    ENCODING = 'UTF8'
    -- 使用干净的默认模板
    TEMPLATE = template0;

-- 切换数据库
\c ai_knowledge

-- 启用 PG 向量库拓展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建知识库向量存储表
CREATE TABLE IF NOT EXISTS vector_knowledge
(
    id        uuid DEFAULT uuid_generate_v4( ) PRIMARY KEY,
    content   TEXT NOT NULL, -- 存储解析后的文本内容
    metadata  JSON,          -- 存储元数据（如文件名等）
    embedding vector(768)    -- 向量字段（维度需与嵌入模型匹配）
);

-- 添加注释
COMMENT ON TABLE vector_knowledge IS '知识库向量表';
COMMENT ON COLUMN vector_knowledge.id IS '唯一标识(UUID)';
COMMENT ON COLUMN vector_knowledge.content IS '存储解析后的文本内容';
COMMENT ON COLUMN vector_knowledge.metadata IS '存储元数据(如文件名等)';
COMMENT ON COLUMN vector_knowledge.embedding IS '向量字段(维度需与嵌入模型匹配)';

-- 创建 HNSW 索引(加速相似性搜索)
CREATE INDEX ON vector_knowledge USING hnsw (embedding vector_cosine_ops);

-- 创建向量知识库关系表
CREATE TABLE IF NOT EXISTS vector_relation
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    file_hash  VARCHAR(64)  NOT NULL,                              -- 文档哈希值
    file_name  VARCHAR(255) NOT NULL,                              -- 文档名称
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);

-- 创建单独索引
CREATE INDEX IF NOT EXISTS idx_file_hash ON vector_relation (file_hash);

-- 添加注释
COMMENT ON TABLE vector_relation IS '向量知识库关系表';
COMMENT ON COLUMN vector_relation.id IS '自增ID';
COMMENT ON COLUMN vector_relation.file_hash IS '文档哈希值';
COMMENT ON COLUMN vector_relation.file_name IS '文档名称';
COMMENT ON COLUMN vector_relation.created_at IS '创建时间';
COMMENT ON COLUMN vector_relation.updated_at IS '更新时间';
