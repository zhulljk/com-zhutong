# User 服务

用户中心服务，提供完整的用户认证和授权功能。

## 项目信息

- **项目名称**: service-user
- **GroupId**: com.zhut
- **版本**: 0.0.1-SNAPSHOT
- **Java 版本**: 21
- **Spring Boot 版本**: 4.0.3
- **Spring Cloud 版本**: 2025.1.1

## 技术栈

### 核心框架
- Spring Boot 4.0.3
- Spring Cloud Consul Discovery
- Spring Security
- Spring Web

### 安全认证
- Spring Security
- OAuth2 Client
- JWT (io.jsonwebtoken 0.12.6)
- 第三方登录支持（GitHub、Google）

### 数据存储
- MyBatis 3.0.4
- Spring Data JDBC
- MariaDB

### 缓存
- Spring Data Redis

### 服务治理
- Consul Discovery
- Spring Boot Actuator

### 容错处理
- Resilience4j Circuit Breaker 2.3.0
- Resilience4j Retry 2.3.0
- Resilience4j Spring Boot3 2.2.0

### 工具类
- Lombok 1.18.38
- Jackson Databind
- AspectJ Weaver

## 主要功能

### 1. 用户认证
- JWT Token 认证
- Access Token + Refresh Token 双 Token 机制
- OAuth2 第三方登录（GitHub、Google）
- 用户名密码登录
- 注册功能

### 2. 安全配置
- Spring Security 安全框架
- JWT 认证过滤器
- OAuth2 登录成功处理器
- 全局异常处理

### 3. 服务治理
- Consul 服务注册与发现
- Spring Boot Admin 监控
- 服务健康检查

### 4. 容错机制
- 熔断器（Circuit Breaker）
- 重试机制（Retry）
- 服务降级处理

## 项目结构

```
user/
├── src/main/java/com/zhut/user/
│   ├── UserApplication.java          # 主启动类
│   ├── common/                       # 公共类
│   │   └── Result.java              # 统一返回结果
│   ├── config/                       # 配置类
│   │   ├── MybatisConfig.java       # MyBatis 配置
│   │   ├── Resilience4jConfig.java  # Resilience4j 配置
│   │   └── SecurityConfig.java      # 安全配置
│   ├── controller/                   # 控制器
│   │   └── AuthController.java      # 认证控制器
│   ├── dto/                          # 数据传输对象
│   │   ├── LoginRequest.java        # 登录请求
│   │   ├── LoginResponse.java       # 登录响应
│   │   └── RegisterRequest.java     # 注册请求
│   ├── entity/                       # 实体类
│   │   └── User.java                # 用户实体
│   ├── exception/                    # 异常类
│   │   └── OAuth2ServiceUnavailableException.java
│   ├── filter/                       # 过滤器
│   │   └── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   ├── handler/                      # 处理器
│   │   ├── GlobalExceptionHandler.java   # 全局异常处理
│   │   └── OAuth2LoginSuccessHandler.java # OAuth2 登录成功处理
│   ├── jwt/                          # JWT 相关
│   │   └── JwtTokenProvider.java    # JWT Token 提供者
│   ├── mapper/                       # MyBatis Mapper
│   │   └── UserMapper.java
│   ├── service/                      # 服务层
│   │   ├── OAuth2UserService.java   # OAuth2 用户服务
│   │   ├── UserDetailsServiceImpl.java
│   │   ├── UserService.java         # 用户服务接口
│   │   └── impl/
│   │       └── UserServiceImpl.java # 用户服务实现
│   └── util/                         # 工具类
│       ├── RedisTokenStore.java     # Redis Token 存储
│       └── SnowflakeIdGenerator.java # 雪花 ID 生成器
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   ├── mapper/                      # MyBatis XML
│   │   ├── .gitkeep
│   │   └── UserMapper.xml
│   └── schema.sql                   # 数据库脚本
└── pom.xml
```

## 配置说明

### 应用配置 (application.yml)
- 服务端口
- 数据库连接
- Redis 连接
- Consul 配置
- OAuth2 客户端配置
- JWT 密钥配置

## 运行方式

### 前置要求
1. Java 21
2. Maven
3. MariaDB/MySQL
4. Redis
5. Consul

### 启动命令
```bash
mvn spring-boot:run
```

### 打包
```bash
mvn clean package -DskipTests
```

## API 接口

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/refresh` - 刷新 Token
- `GET /api/auth/logout` - 用户登出

### OAuth2 登录
- `GET /oauth2/authorization/github` - GitHub 登录
- `GET /oauth2/authorization/google` - Google 登录

## 数据库

### 用户表
- 用户 ID（雪花算法生成）
- 用户名
- 密码（加密）
- 邮箱
- 第三方登录信息
- 创建时间
- 更新时间

## 依赖服务

1. **Consul** - 服务注册与发现
2. **Redis** - Token 存储和缓存
3. **MariaDB/MySQL** - 用户数据存储
4. **Spring Boot Admin** - 服务监控

## 注意事项

1. 需要配置 JWT 密钥
2. 需要配置 OAuth2 客户端 ID 和密钥
3. 数据库表结构参考 schema.sql
4. Redis 需要配置密码（如果启用）
5. Consul 需要运行在服务发现模式

## 开发规范

1. 包名统一使用 `com.zhut.user`
2. 实体类使用 Lombok 简化代码
3. 统一使用 Result 返回结果
4. 异常统一由 GlobalExceptionHandler 处理
5. 所有接口进行 JWT 认证（公开接口除外）
