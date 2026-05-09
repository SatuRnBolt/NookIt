# nookit-backend

Nookit 自习座位预约系统的 Java 后端服务。

> 本仓库目前仅完成 **基础架构骨架**（统一响应 / 异常 / 工具 / 安全 / 配置），具体业务模块按 Sprint 计划逐步落地。

## 技术栈

| 层级 | 选型 |
|---|---|
| JDK | 17 |
| 框架 | Spring Boot 3.2.x |
| 持久层 | MyBatis-Plus 3.5.x |
| 数据库 | MySQL 8 |
| 缓存 / 锁 | Redis 7 + Redisson |
| 安全 | Spring Security 6 + JJWT |
| 消息队列 | RabbitMQ |
| API 文档 | SpringDoc OpenAPI 2.x |
| 工具库 | Lombok / MapStruct / Hutool |
| 构建 | Maven |

## 启动

### 1. 准备依赖中间件

需要在本机或开发环境运行：

- MySQL 8（使用 `mysql/init.sql` 初始化）
- Redis 7
- RabbitMQ 3（可选，未上线通知模块前可先关闭相关 listener）

### 2. 填写数据库连接

`src/main/resources/application-dev.yml` 中**故意留空**了数据库名 / 账号 / 密码，需要在启动前通过环境变量或直接修改文件填入。推荐用环境变量：

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=study_room        # 与 mysql/init.sql 中的库名一致
export DB_USERNAME=your_username
export DB_PASSWORD=your_password

export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=

export NOOKIT_JWT_SECRET=$(openssl rand -hex 32)
```

### 3. 运行

```bash
cd backend
./mvnw spring-boot:run
# 或
./mvnw clean package
java -jar target/nookit-backend.jar
```

启动后默认监听 `8080`，context-path = `/api`。

### 4. 自测

| 用途 | URL |
|---|---|
| 健康检查 | http://localhost:8080/api/ping |
| 业务异常示例 | http://localhost:8080/api/ping/error?type=biz |
| 未捕获异常示例 | http://localhost:8080/api/ping/error?type=oops |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api/v3/api-docs |

## 目录结构

```
src/main/java/com/nookit
├── NookitApplication.java          # 启动类
├── common/                         # 基类框架（与具体业务无关）
│   ├── api/                        # Result / PageResult / PageRequest / ResultCode
│   ├── exception/                  # 业务异常 + 全局异常处理器
│   ├── domain/                     # BaseEntity（id/createdAt/updatedAt/deletedAt）
│   ├── controller/                 # BaseController（取登录用户、IP）
│   ├── service/                    # BaseService / BaseServiceImpl
│   ├── mapper/                     # Mapper 扩展点
│   ├── enums/                      # BaseEnum / StatusEnum
│   ├── constant/                   # 常量（缓存 key、安全、通用）
│   ├── util/                       # JWT / 密码 / Redis / 日期 / JSON / IP
│   ├── annotation/                 # @CurrentUser / @OperationLog / @RateLimit
│   ├── aspect/                     # 注解切面
│   └── validation/                 # 自定义校验注解
├── config/                         # 框架配置
│   ├── WebMvcConfig                # 注册参数解析器
│   ├── CurrentUserArgumentResolver # 解析 @CurrentUser
│   ├── TraceIdFilter               # 写入 MDC traceId
│   ├── JacksonConfig               # Long → String、统一时间格式
│   ├── CorsConfig                  # CORS 配置项
│   ├── MybatisPlusConfig           # 分页 + 自动填充
│   ├── RedisConfig                 # RedisTemplate 序列化
│   ├── AsyncConfig                 # 线程池 + 调度
│   ├── SwaggerConfig               # OpenAPI
│   ├── RabbitMQConfig              # MQ 转换器
│   ├── SecurityConfig              # Spring Security 主配置
│   └── JwtAuthenticationFilter     # JWT 认证过滤器
├── security/                       # 认证授权
│   ├── UserPrincipal               # Spring Security UserDetails
│   └── PermissionEvaluator         # @PreAuthorize("@perm.has('xxx')")
└── modules/                        # 业务模块（按 Sprint 推进）
    └── ping/                       # 框架自测，业务上线后可移除
```

## 约定

### 统一响应

所有 Controller 返回 `Result<T>` 或 `Result<PageResult<T>>`：

```java
return Result.success(data);
return Result.error(ResultCode.RESERVATION_TIME_CONFLICT);
```

### 抛异常代替返回 error

业务侧不要手写 error 返回，统一抛 `BusinessException` 或其子类，由 `GlobalExceptionHandler` 翻译：

```java
throw new ResourceNotFoundException(ResultCode.ROOM_NOT_FOUND);
throw new ConflictException(ResultCode.RESERVATION_TIME_CONFLICT);
```

### 实体审计字段

业务实体继承 `BaseEntity`，`createdAt` / `updatedAt` / `deletedAt` 由 `MybatisPlusConfig.metaObjectHandler` 自动填充，业务代码不要手动 set。

### 鉴权注解

```java
@PreAuthorize("@perm.has('reservation:write')")
public Result<Void> create(@CurrentUser Long userId, @RequestBody @Valid CreateReservationDTO dto) { ... }
```

### 错误码规范

参见 `common/api/ResultCode.java`：每个业务域分配 1 万号段。

### 缓存 Key 命名

参见 `common/constant/CacheKeyConstants.java`：统一前缀 `nookit:`，按 `domain:purpose:id` 命名。

## 下一步

按 `story/sprint-plan.md` 逐 Sprint 实现业务模块：

- Sprint 1：`modules/auth`、`modules/user`、`modules/admin`（RBAC）
- Sprint 2：`modules/room`、`modules/seat`
- Sprint 3：`modules/reservation`
- Sprint 4：`modules/checkin`、`modules/notification`
- Sprint 5：`modules/ai`、统计
- Sprint 6：DevOps & 优化
