# Menu 服务

菜单权限管理服务，提供菜单的 CRUD 操作和菜单树形结构管理。

## 项目信息

- **项目名称**: service-menu
- **GroupId**: com.zhut
- **版本**: 0.0.1-SNAPSHOT
- **Java 版本**: 21
- **Spring Boot 版本**: 4.0.3

## 技术栈

### 核心框架
- Spring Boot 4.0.3
- Spring Web
- MyBatis

### 数据存储
- MyBatis
- Spring Data JDBC
- MariaDB/MySQL

### 工具类
- Lombok
- Jackson Databind
- Snowflake ID 生成器

## 主要功能

### 1. 菜单管理
- 菜单的增删改查
- 菜单树形结构展示
- 菜单层级管理
- 菜单排序

### 2. 菜单类型
- 目录
- 菜单项
- 按钮权限
- 外部链接

### 3. 权限控制
- 菜单权限标识
- 按钮权限标识
- 角色关联（预留）

## 项目结构

```
menu/
├── src/main/java/com/zhut/menu/
│   ├── MenuApplication.java          # 主启动类
│   ├── common/                       # 公共类
│   │   └── Result.java              # 统一返回结果
│   ├── config/                       # 配置类
│   │   └── MybatisConfig.java       # MyBatis 配置
│   ├── controller/                   # 控制器
│   │   └── MenuController.java      # 菜单控制器
│   ├── dto/                          # 数据传输对象
│   │   ├── MenuCreateRequest.java   # 菜单创建请求
│   │   ├── MenuTreeVO.java          # 菜单树形视图
│   │   └── MenuUpdateRequest.java   # 菜单更新请求
│   ├── entity/                       # 实体类
│   │   └── Menu.java                # 菜单实体
│   ├── handler/                      # 处理器
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── mapper/                       # MyBatis Mapper
│   │   └── MenuMapper.java
│   ├── service/                      # 服务层
│   │   ├── MenuService.java         # 菜单服务接口
│   │   └── impl/
│   │       └── MenuServiceImpl.java # 菜单服务实现
│   └── util/                         # 工具类
│       └── SnowflakeIdGenerator.java # 雪花 ID 生成器
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   └── mapper/                      # MyBatis XML
│       └── MenuMapper.xml
├── sql/
│   └── menu.sql                     # 数据库脚本
└── pom.xml
```

## 数据模型

### Menu 实体
- **id**: Long (雪花算法生成)
- **parentId**: Long (父菜单 ID)
- **name**: String (菜单名称)
- **path**: String (菜单路径)
- **component**: String (组件路径)
- **permission**: String (权限标识)
- **type**: Integer (菜单类型：0-目录，1-菜单，2-按钮，3-外链)
- **icon**: String (菜单图标)
- **sort**: Integer (排序)
- **createTime**: Date (创建时间)
- **updateTime**: Date (更新时间)

## 配置说明

### 应用配置 (application.yml)
- 服务端口
- 数据库连接
- MyBatis 配置
- 日志配置

## 运行方式

### 前置要求
1. Java 21
2. Maven
3. MariaDB/MySQL

### 启动命令
```bash
mvn spring-boot:run
```

### 打包
```bash
mvn clean package -DskipTests
```

## API 接口

### 菜单管理
- `GET /api/menus` - 获取所有菜单（树形结构）
- `GET /api/menus/{id}` - 获取菜单详情
- `POST /api/menus` - 创建菜单
- `PUT /api/menus/{id}` - 更新菜单
- `DELETE /api/menus/{id}` - 删除菜单

### 菜单树
- `GET /api/menus/tree` - 获取菜单树形结构
- `GET /api/menus/tree/{parentId}` - 获取指定父级下的菜单树

## 数据库

### 菜单表 (menu)
```sql
CREATE TABLE menu (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    permission VARCHAR(100),
    type TINYINT NOT NULL DEFAULT 0,
    icon VARCHAR(50),
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type)
);
```

## 业务规则

### 菜单类型
- **0 - 目录**: 顶级菜单，可以包含子菜单
- **1 - 菜单项**: 具体的功能页面
- **2 - 按钮**: 页面中的操作按钮
- **3 - 外链**: 外部链接地址

### 权限标识
- 格式：`模块：操作`
- 示例：`system:user:add` (系统模块：用户管理：新增)

### 树形结构
- 根菜单的 parentId 为 0
- 支持无限层级
- 自动计算层级路径

## 开发规范

1. 包名统一使用 `com.zhut.menu`
2. 实体类使用 Lombok 简化代码
3. 统一使用 Result 返回结果
4. 异常统一由 GlobalExceptionHandler 处理
5. ID 统一使用雪花算法生成
6. 菜单树形结构在 Service 层组装

## 扩展功能（预留）

1. **角色关联**: 菜单与角色的关联关系
2. **用户权限**: 根据用户角色动态加载菜单
3. **国际化**: 菜单名称的多语言支持
4. **菜单缓存**: Redis 缓存菜单数据

## 注意事项

1. 删除菜单前需要检查是否有子菜单
2. 修改菜单类型需要谨慎，可能影响权限判断
3. 菜单路径需要符合前端路由规范
4. 权限标识需要全局唯一
