# Docker 部署运行 PG 向量库容器
# 需要将 /path/to/data 换成实际路径地址
docker run -d --name pgvector -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=123456 -v /path/to/data:/var/lib/postgresql/data  pgvector/pgvector:pg17
