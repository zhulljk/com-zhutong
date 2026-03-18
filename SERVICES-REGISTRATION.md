# 服务注册指南

本指南说明如何将 user 和 menu 服务注册到 center-server。

## 服务架构

```
┌─────────────────┐
│   Consul        │
│   (8500)        │
│   服务注册中心   │
└────────┬────────┘
         │
    ┌────┴────┬────────────┬────────────┐
    │         │            │            │
┌───▼───┐ ┌──▼────┐ ┌─────▼────┐ ┌────▼─────┐
│Center │ │ User  │ │   Menu   │ │ 其他服务 │
│Server │ │Service│ │ Service  │ │          │
│:8090  │ │ :8080 │ │  :8081   │ │          │
└───────┘ └───────┘ └──────────┘ └──────────┘
```

## 已完成的配置

### 1. Center Server (center-server)
- ✅ 服务名：`center-server`
- ✅ 端口：`8090`
- ✅ Consul 配置：`localhost:8500`
- ✅ 启用了 `@EnableDiscoveryClient` 和 `@EnableAdminServer`
- ✅ Actuator 端点已暴露

### 2. User Service (user)
- ✅ 服务名：`user`
- ✅ 端口：`8080` (默认)
- ✅ Consul 配置：`localhost:8500`
- ✅ 启用了 `@EnableDiscoveryClient`
- ✅ Actuator 健康检查已配置
- ✅ 数据库连接已配置
- ✅ Redis 连接已配置
- ✅ OAuth2 配置已准备
- ✅ Resilience4j 熔断器已配置

### 3. Menu Service (menu)
- ✅ 服务名：`menu`
- ✅ 端口：`8081`
- ✅ Consul 配置：`localhost:8500`
- ✅ 启用了 `@EnableDiscoveryClient`
- ✅ Actuator 健康检查已配置
- ✅ 数据库连接已配置

## 启动步骤

### 前置要求

1. **安装并启动 Consul**
```bash
# Windows PowerShell
consul agent -dev -ui

# 或使用 Docker
docker run -d -p 8500:8500 -p 8600:8600/udp consul:latest
```

2. **确保数据库可用**
- MariaDB/MySQL: `192.168.240.133:30930`
- Redis: `192.168.240.133:30379`

### 启动顺序

#### 1. 启动 Center Server
```bash
cd center-server
mvn spring-boot:run
```

**验证启动成功：**
- 访问 http://localhost:8090/actuator/health
- 访问 Consul UI: http://localhost:8500/ui/dc1/services

#### 2. 启动 User Service
```bash
cd user
mvn spring-boot:run
```

**验证启动成功：**
- 访问 http://localhost:8080/actuator/health
- 在 Consul UI 中查看 `user` 服务状态

#### 3. 启动 Menu Service
```bash
cd menu
mvn spring-boot:run
```

**验证启动成功：**
- 访问 http://localhost:8081/actuator/health
- 在 Consul UI 中查看 `menu` 服务状态

## 服务注册验证

### 方法 1: Consul UI
1. 打开浏览器访问：http://localhost:8500/ui/dc1/services
2. 查看服务列表，应该包含：
   - `center-server` (1 个实例)
   - `user` (1 个实例)
   - `menu` (1 个实例)

### 方法 2: Consul API
```bash
# 查看所有服务
curl http://localhost:8500/v1/catalog/services

# 查看 user 服务实例
curl http://localhost:8500/v1/catalog/service/user

# 查看 menu 服务实例
curl http://localhost:8500/v1/catalog/service/menu

# 查看 center-server 服务实例
curl http://localhost:8500/v1/catalog/service/center-server
```

### 方法 3: Spring Boot Admin
如果启用了 Spring Boot Admin：
- 访问：http://localhost:8090
- 在 Applications 页面查看所有注册的服务

## 服务间调用

### User Service 调用示例
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @GetMapping("/with-menus/{userId}")
    public UserWithMenus getUserWithMenus(@PathVariable Long userId) {
        // 通过服务名调用 menu 服务
        String menuServiceUrl = "http://menu/api/menus/user/" + userId;
        return restTemplate.getForObject(menuServiceUrl, UserWithMenus.class);
    }
}
```

### Menu Service 调用示例
```java
@RestController
@RequestMapping("/api/menus")
public class MenuController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @GetMapping("/user/{userId}")
    public List<Menu> getUserMenus(@PathVariable Long userId) {
        // 通过服务名调用 user 服务获取用户信息
        String userServiceUrl = "http://user/api/users/" + userId;
        return restTemplate.getForObject(userServiceUrl, List.class);
    }
}
```

## 健康检查配置

### User Service
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    redis:
      enabled: true
```

### Menu Service
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
```

### Center Server
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

## 常见问题

### 1. 服务注册失败
**现象**: 服务启动后在 Consul 中看不到
**原因**: 
- Consul 未启动
- Consul 地址配置错误
- 网络不通

**解决**:
```bash
# 检查 Consul 是否运行
consul members

# 检查配置文件中的 Consul 地址
cat user/src/main/resources/application.yml | grep consul
```

### 2. 健康检查失败
**现象**: 服务在 Consul 中显示但不健康
**原因**:
- Actuator 未启用
- 健康检查路径错误
- 数据库/Redis 连接失败

**解决**:
```bash
# 检查 Actuator 端点
curl http://localhost:8080/actuator/health

# 检查数据库连接
# 查看启动日志中的数据库连接信息
```

### 3. 端口冲突
**现象**: 服务启动失败，提示端口被占用
**解决**: 修改配置文件中的端口号
```yaml
server:
  port: 8080  # 修改为其他端口
```

### 4. 服务名冲突
**现象**: 多个服务使用相同的服务名
**解决**: 确保每个服务的 `spring.application.name` 唯一

## 负载均衡配置

### 添加 Spring Cloud LoadBalancer
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

### 配置 RestTemplate
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

## 服务下线

### 正常下线
服务关闭时会自动从 Consul 注销

### 强制下线
如果服务异常退出，Consul 会在健康检查失败后自动移除服务

### 手动移除
```bash
# 通过 Consul API 移除服务实例
curl -X PUT http://localhost:8500/v1/catalog/deregister
```

## 监控与日志

### Consul 日志
```bash
# 查看 Consul 日志
consul monitor
```

### 服务日志
每个服务的启动日志会显示：
```
Registered with Consul
```

### Spring Boot Admin 监控
- 实时查看服务状态
- 查看服务指标
- 查看服务日志
- 线程 dump 分析

## 下一步

1. **添加 Feign 客户端** (可选)
   - 使用 OpenFeign 简化服务间调用
   
2. **配置网关** (可选)
   - 添加 Spring Cloud Gateway
   - 统一入口和路由
   
3. **配置链路追踪** (可选)
   - 添加 Spring Cloud Sleuth
   - 集成 Zipkin

4. **增强安全性** (可选)
   - 配置 Consul ACL
   - 启用 HTTPS

## 总结

✅ **已完成**:
- Center Server 配置完成
- User Service 配置完成
- Menu Service 配置完成
- Consul 服务发现已配置
- 健康检查已配置
- Actuator 端点已暴露

🎯 **启动顺序**:
1. Consul
2. Center Server
3. User Service
4. Menu Service

📊 **验证方式**:
- Consul UI
- Actuator 端点
- Spring Boot Admin

所有服务已成功配置并准备注册到 Consul！
