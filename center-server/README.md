# Center Server

中心服务，提供服务注册发现和服务监控功能。

## 项目信息

- **项目名称**: center-server
- **GroupId**: com.zhut
- **版本**: 0.0.1-SNAPSHOT
- **Java 版本**: 21
- **Spring Boot 版本**: 4.0.3

## 技术栈

### 核心框架
- Spring Boot 4.0.3
- Spring Web

### 服务治理
- Spring Cloud Consul Discovery
- Spring Boot Actuator

### 监控
- Spring Boot Admin (预留)

## 主要功能

### 1. 服务注册与发现
- 基于 Consul 的服务注册
- 服务实例发现
- 服务健康检查
- 服务注销

### 2. 服务监控
- Spring Boot Actuator 端点
- 服务健康状态监控
- 服务指标收集

### 3. 配置中心（预留）
- Consul KV 存储
- 动态配置刷新

## 项目结构

```
center-server/
├── src/main/java/com/zhut/center/
│   ├── CenterServerApplication.java  # 主启动类
│   └── ...
├── src/main/resources/
│   └── application.yml              # 应用配置
└── pom.xml
```

## 配置说明

### 应用配置 (application.yml)
- 服务端口
- Consul 配置
- Actuator 端点配置
- 日志配置

## 运行方式

### 前置要求
1. Java 21
2. Maven
3. Consul

### 启动命令
```bash
mvn spring-boot:run
```

### 打包
```bash
mvn clean package -DskipTests
```

## 依赖服务

### Consul
- **作用**: 服务注册与发现
- **端口**: 8500 (HTTP), 8600 (DNS)
- **配置**:
  - host: localhost
  - port: 8500
  - scheme: http

## 服务注册

### 注册到 Consul
服务启动后会自动注册到 Consul，注册信息包括：
- 服务 ID
- 服务名称
- 服务地址
- 服务端口
- 健康检查配置

### 健康检查
- **检查类型**: HTTP 检查
- **检查路径**: /actuator/health
- **检查间隔**: 10 秒
- **超时时间**: 5 秒

## Actuator 端点

### 内置端点
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 指标数据
- `/actuator/env` - 环境变量
- `/actuator/beans` - Spring Beans 信息

### 自定义端点（预留）
- `/actuator/custom` - 自定义监控端点

## 服务发现

### 发现其他服务
通过 Consul 可以发现其他注册的服务：
- User 服务
- Menu 服务
- 其他微服务

### 服务调用
使用 Spring Cloud LoadBalancer 进行负载均衡调用

## 控制台访问

### Consul UI
- **地址**: http://localhost:8500
- **功能**:
  - 查看注册的服务
  - 查看服务健康状态
  - 查看 KV 配置
  - 管理服务实例

### Spring Boot Admin（预留）
- **地址**: http://localhost:8080
- **功能**:
  - 服务监控
  - 日志查看
  - 线程 dump
  - 内存监控

## 开发规范

1. 包名统一使用 `com.zhut.center`
2. 保持项目简洁，只包含必要的依赖
3. 配置集中管理在 application.yml
4. 启用所有必要的 Actuator 端点

## 注意事项

1. **Consul 必须运行**: 服务启动前需要确保 Consul 已启动
2. **端口配置**: 默认端口 8080，避免与其他服务冲突
3. **健康检查**: 确保 Actuator 端点可访问
4. **服务注销**: 服务关闭时会自动从 Consul 注销

## 扩展功能（预留）

### 1. Spring Boot Admin Server
添加 Admin Server 功能，集中监控所有服务：
```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
</dependency>
```

### 2. Consul Config
使用 Consul 作为配置中心：
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-config</artifactId>
</dependency>
```

### 3. 配置刷新
支持配置的动态刷新：
```java
@RefreshScope
@RestController
public class ConfigController {
    // ...
}
```

## 部署说明

### 本地开发
1. 启动 Consul
2. 启动 Center Server
3. 启动其他微服务

### 生产环境
1. Consul 集群部署
2. Center Server 高可用部署
3. 配置负载均衡
4. 启用 SSL 安全访问

## 监控告警（预留）

### 健康告警
- 服务下线告警
- 健康检查失败告警
- CPU/内存告警

### 日志聚合
- 集成 ELK
- 日志集中收集
- 日志分析

## 常见问题

### 1. Consul 连接失败
**原因**: Consul 未启动或配置错误  
**解决**: 检查 Consul 服务和配置文件

### 2. 服务注册失败
**原因**: 服务名称冲突或端口占用  
**解决**: 修改服务名称或端口

### 3. 健康检查失败
**原因**: Actuator 未启用或路径错误  
**解决**: 检查 Actuator 配置
