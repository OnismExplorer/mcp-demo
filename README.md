# MCP Demo

[![Java Version](https://img.shields.io/badge/java-17%2B-orange?logo=java)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.4.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0%20M6-blue?logo=spring)](https://docs.spring.io/spring-ai/reference/index.html)

最近 Spring AI 发布了 1.0.0-M6，引入了一个新特性`MCP`(Model Context Protocol)，关于这个概念在这里就不过多赘述，文档介绍的比较清楚：<br>
- [MCP 中文文档](https://mcp-docs.cn/quickstart)
- [Spring AI](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)

简单来说，本地部署的 LLM 或调用三方 AI API的功能是部分缺失的(无法联网，无法访问本地附件等)，MCP 就是通过给大模型 LLM 提供各种各样的第三方工具(封装为工具类/函数)，赋予大模型 LLM 各种各样的能力(例如，访问本地文件系统的能力、访问数据库的能力、发送邮件的能力等等) <br>

跟着官方文档和网上的资料，结合着 AI (DeepSeek)，自己写了个 Demo 玩玩( Server 与 Client 写在一起的)。
## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
    - [环境要求](#环境要求)
    - [安装步骤](#安装步骤)

## 功能特性
大模型方面采用 DeepSeek V3 模型(使用官方 API)，通过整合自己封装的工具使其具备下面的能力：
- 获取时间<br>
![img.png](src/main/resources/static/getTime.png)
- 读取本地文件系统<br>
(Spring AI Alibaba Examples 提供的样例，仓库：[https://github.com/springaialibaba/spring-ai-alibaba-examples/tree/main](https://github.com/springaialibaba/spring-ai-alibaba-examples/tree/main))
![img.png](src/main/resources/static/getFile.png)
- 数据库 SQL 操作(目前只允许查询)<br>
![img.png](src/main/resources/static/getDatabase.png)
- 发送邮件(给指定邮箱发送邮件)
![img.png](src/main/resources/static/senEmail.png)
- 获取系统资源详情/使用率/监控系统<br>
(QQ邮箱渲染问题，可自动忽略...)
![img.png](src/main/resources/static/monitor1.png)
![img.png](src/main/resources/static/monitor2.png)
- ...(联网功能等等，可继续扩展)

## 技术栈

- **后端框架**: Spring Boot 3.x / Spring AI 1.0.0-M6
- **数据库**: MySQL 8.0 / PostgreSQL 14 / Oracle(使用多数据源策略模式，需要多少个数据源可自行添加相关数据源依赖即可)
- **API 文档**: Swagger 3
- **构建工具**: Maven
- **其他技术**: JDBC / JMX / Java Email

# 项目结构
```text
src/
├── main/
│   ├── java/
│   │   └── cn/onism/mcp/
│   │       ├── common/        # 通用类
│   │       ├── config/    # 配置类
│   │       ├── constants/       # 常量类
│   │       ├── controller/    # REST API
│   │       ├── exception/         # 自定义异常类
│   │       ├── handler/         # 处理器类
│   │       ├── tool/         # (LLM)封装工具类
│   │       └── McpDemoApplication.java # 启动类
│   └── resources/
│       ├── application.yml    # 主配置文件
│       ├── static/           # 静态资源
│       └── .env/        # 环境配置文件
└── test/                      # 单元测试
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL  / 其他数据库
- Git

### 安装步骤

1. 克隆仓库：
   ```bash
   git clone https://github.com/yourusername/projectname.git

2. 配置数据源
    ```yaml
   spring:
     datasources:
      datasource:
        - id: mysql
          type: mysql
          url: jdbc:mysql://localhost:5206/power_buckle?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
          maxPoolSize: 15
        - id: postgres
          type: postgres
          url: jdbc:postgresql://localhost:5432/friends
          username: root
          password: 123456
          driver-class-name: org.postgresql.Driver
          max-pool-size: 10
        # 其他数据源，可继续添加，需要保证 id 全局唯一(数据源)，后续与大模型对话会用到
    ```
3. 配置邮箱(选配，用不到就不配)<br>
    修改 `resources` 中的 `.env` 环境变量文件，替换其中的 $$$ 内容
    ```dotenv
    EMAIL_ADDRESS=$$$EMAIL_ADDRESS$$$
    EMAIL_PASSWORD=$$$EMAIL_PASSWORD$$$
    ```
   如果用的不是 QQ 邮箱，则还需要修改`application.yml`文件内容(按照需要修改) 
    ```yaml
      spring:
        mail:
        # 下面这个是QQ邮箱host ， 企业邮箱 : smtp.exmail.qq.com
          host: smtp.qq.com
        # tencent mail port  这个是固定的
          port: 465
          properties:
            mail:
              smtp:
                socketFactory:
                  port: 465
                  class: javax.net.ssl.SSLSocketFactory
                ssl:
                  enable: true
    ```
4. 配置大模型 API<br>
   修改 `resources` 中的 `.env` 环境变量文件，用真实数据替换其中的 $$$ 内容
    ```dotenv
    AI_BASE_URL=$$$AI_BASE_URL$$$
    # AI 密钥，可通过 https://platform.deepseek.com/api_keys 充值获取 deepseek 官方 api key
    AI_API_KEY=$$$AI_API_KEY$$$
    # DeepSeek v3 聊天模型
    AI_MODEL=$$$AI_MODEL$$$
    ```
5. 构建项目
    ```bash
    mvn clean install
    ```
6. 运行应用
    ```bash
    java -jar target/mcp-demo-1.0-SNAPSHOT.jar
    ```
7. 访问应用
    ```bash
    http://localhost:8089/chat?message=hi
    ```
