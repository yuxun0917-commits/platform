# Platform 快速开发脚手架

基于 **Spring Boot 3.2.x + MyBatis-Plus 3.5.x + Sa-Token + RabbitMQ** 的企业级 Maven 多模块后端脚手架，开箱即用。基础包名 `com.platform`，统一返回 `Result<T>` / `PageResult<T>`。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 基础框架 | Spring Boot 3.2.5（JDK 17） |
| 安全框架 | Sa-Token 1.45.0（会话 / 权限，token 默认名 `satoken`） |
| 构建工具 | Maven 3.9+ |
| ORM | MyBatis-Plus 3.5.5（分页 / 乐观锁 / 自动填充），Druid 连接池 |
| 数据库 | MySQL 8.0.33 + Redis（Lettuce） |
| 消息队列 | RabbitMQ（spring-boot-starter-amqp，Jackson 序列化） |
| 对象转换 | MapperFacade（Orika 3.2.1，由 starter 自动装配提供） |
| 定时任务 | Quartz（spring-boot-starter-quartz） |
| 文件存储 | 多后端可切换：本地 / 阿里云 OSS / 腾讯 COS / MinIO（策略模式）+ 分片上传 |
| 导出 | EasyExcel 3.3.4 |
| 服务监控 | OSHI 6.8.2（CPU / 内存 / 磁盘 / JVM）+ Redis INFO |
| 限流 | Redisson 3.27.2 |
| 工具库 | Lombok / Hutool / TransmittableThreadLocal / Spring Security Crypto（BCrypt） |
| 接口文档 | SpringDoc OpenAPI 2.3.0（Swagger3，路径 `/swagger-ui.html`） |
| 微服务组件 | Spring Cloud OpenFeign 2023.0.x（按需引入） |
| 日志 | Logback（按天滚动）+ AOP 敏感字段脱敏 |

## 模块说明

```
platform
├── platform-parent       父模块（pom），统一依赖版本管理
├── platform-common       通用模块：Result/PageResult/ErrorCode/BusinessException/BaseEntity/
│                          RedisConstant/工具类/事件(event)/聚合载体(bo)/枚举(enums)
├── platform-framework    框架配置：MyBatisPlus/Redis/Jackson/CORS/Sa-Token/GlobalExceptionHandler/
│                          RabbitMQConfig/security(StpInterfaceImpl)/SpringDoc
├── platform-starter      自动装配模块：文件上传/线程池/限流/Redis/密码加密/MapperFacade/
│                          多存储 FileStorage（Local/Oss/Cos/Minio）
├── platform-component    可复用业务组件：各模块的 XxxComponent（缓存/事务/聚合）、
│                          TreeBuilder/PageExport/Dict/FileProcess/ServerComponent 等
├── platform-service      业务模块：Entity/Mapper/Service/RabbitMQ 生产者消费者
├── platform-admin        启动模块（可运行）：Controller/VO/Converter/启动类/配置文件
├── platform-generator    代码生成器：基于表结构一键生成 Entity/Mapper/Service/Controller
└── sql                   RBAC 及业务建表脚本（含菜单初始化数据）
```

> 实体类统一以 `Sys` 前缀命名（如 `SysUser`、`SysMenu`），Controller 类以 `Sys` 前缀命名（如 `SysUserController`）；**路径**使用功能名（见下方 API 规范）。

### 依赖方向（无循环依赖）

```
platform-admin
   ├── platform-component ──┬── platform-service ──> platform-common
   │                        ├── platform-framework ─┬── platform-service ──> platform-common
   │                        │                       └── platform-starter ──> platform-common
   │                        └── platform-starter ──> platform-common
   ├── platform-service ──> platform-common
   ├── platform-framework
   └── platform-starter

platform-generator   独立模块（仅依赖 mybatis-plus-generator / freemarker / mysql / hutool）
```

要点：`platform-component` 可注入 `platform-service` / `platform-framework` / `platform-starter`，但 **`component` 不依赖 `platform-admin`**，因此不能引用 admin 的 VO，跨模块对象转换须留在 admin 展示层。

## platform-starter 自动装配说明

`platform-starter` 基于 Spring Boot 3.x 自动装配机制，通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置类，**引入依赖即自动生效**，业务方无需手动编写配置类。

| 自动配置类 | 配置前缀 | 开关属性 | 默认值 |
| --- | --- | --- | --- |
| FileUploadAutoConfiguration | file.upload | file.upload.enabled | true |
| ThreadPoolAutoConfiguration | platform.thread-pool | platform.thread-pool.enabled | true |
| RateLimiterAutoConfiguration | platform.rate-limiter | platform.rate-limiter.enabled | true |
| RedisAutoConfiguration | spring.data.redis | — | — |
| PasswordEncoderAutoConfiguration | — | — | 提供 `BCryptPasswordEncoder` Bean |
| MapperFacadeAutoConfiguration | — | — | 提供 `MapperFacade`（Orika）Bean |

每个配置类均使用 `@ConditionalOnProperty` 控制启停，在 `application.yml` 中按需开启 / 关闭。

## 环境准备

1. JDK 17
2. Maven 3.9+
3. MySQL 8.0（执行 `sql/` 下对应建表脚本建库建表）
4. Redis
5. RabbitMQ（管控台默认 `http://localhost:15672`，guest/guest）
6. 可选：阿里云 OSS / 腾讯 COS / MinIO（启用对应存储后端时）

## 启动步骤（IDEA）

1. **导入项目**：IDEA → Open → 选择 `platform` 根目录（识别根 `pom.xml`）。
2. **安装依赖**：等待 Maven 自动下载，或执行 `mvn clean install -DskipTests`。
3. **修改配置**：编辑 `platform-admin/src/main/resources/application-dev.yml`，修改 MySQL / Redis / RabbitMQ 连接信息。
4. **初始化数据库**：在 MySQL 中执行 `sql/` 下的建表脚本（如 `rbac.sql`、`menu_init.sql`）。
5. **启动应用**：运行 `platform-admin` 模块中的 `com.platform.PlatformApplication` 主类。
6. **访问 Swagger**：浏览器打开 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)。

## 验证 RabbitMQ 连接成功

1. **查看启动日志**：控制台出现 `Created new connection` 且无异常，即表示连接成功。
2. **调用接口触发消息**：新增用户等写操作后观察日志：
   - 生产者日志：`[MQ生产者] 发送用户消息, correlationId=...`
   - 消费者日志：`[MQ消费者] 收到用户消息, message=...`
3. **RabbitMQ 管控台**：访问 `http://localhost:15672`（guest/guest），Queues 页面可见对应队列。

## API 规范

### 路径风格

- 根路径前缀为**功能名**（非实体名，不带 `Sys`）：`/user`、`/role`、`/menu`、`/dept`、`/post`、`/dict`、`/dictItem`、`/notice`、`/job`、`/jobLog`、`/storage-config`、`/attachment`、`/monitor`、`/auth`、`/captcha`。
- 日志 / 配置类保留 `sys` 前缀：`/sysLog`、`/sysLoginLog`、`/sysConfig`。
- 登录认证走 `/auth/login`、`/auth/logout`；图形验证码走 `/captcha/get`。

### 标准子资源（约定俗成）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/page` | 分页列表（`page` + `pageSize` + 过滤条件） |
| GET | `/select-list` | 下拉选择列表（远程搜索用） |
| GET | `/view` | 详情（按 `id`） |
| GET | `/enums` | 枚举下拉 |
| POST | `/add` | 新增 |
| POST | `/edit` | 编辑 |
| POST | `/delete` | 删除（单参 `id`） |
| POST | `/editStatus` | 状态切换（启用 / 停用，单参 `id`） |
| POST | `/sort` | 同级排序（`parentId` + `ids`，扁平结构用 `startOrder` + `ids`） |

功能特有接口示例：`/user/info`（当前用户信息 + 角色 + 权限 + 菜单树）、`/user/changePassword`、`/role/assignMenus`、`/menu/tree`、`/storage-config/set-default`、`/attachment/chunk/*`（分片上传）、`/monitor/server`、`/job/changeStatus`、`/job/run`。

### 统一返回

- 所有接口返回 `Result<T>`，分页返回 `PageResult<T>`，字段 `code` / `msg` / `data`。
- **成功 `code = 200`**，前端据此判断；业务异常由 `GlobalExceptionHandler` 统一封装返回。
- 前端 HTTP 拦截器会对超长整数（雪花 ID，16 位以上）自动转字符串，避免精度丢失。

## 核心开发规范

- **统一返回**：所有接口返回 `Result<T>` / `PageResult<T>`，成功 `code = 200`。
- **VO 隔离**：Controller 仅接收 / 返回 VO（`platform-admin`），Service 操作 Entity（`platform-service`）；跨模块对象转换受模块边界约束（见下）。
- **对象转换用 MapperFacade（Orika）**：由 `platform-starter` 自动装配提供 `MapperFacade` Bean，注入即用；**禁止 `BeanUtils.copyProperties`**。
- **事务统一走 Component**：所有写操作的事务必须放在 `XxxComponent#doSomethingInTransactional` 中执行，**禁止在 Controller / Service 直接写 `@Transactional`**；无 component 的模块需先建 `XxxComponent`。
- **缓存逻辑下沉到 Component**：cache-aside（读缓存 → 未命中查库 → 回填）在 Component 内完成，Component 只读写 **JSON 字符串**；Controller 负责反序列化与 VO 转换。因 `component` 不依赖 `admin`，**不能引用 admin 的 VO**，跨模块转换留在 admin 展示层。
- **缓存 Key 约定**：统一前缀（如 `platform:`），JSON 字符串存储；缓存失效通过 Spring 事件（`UserCacheDeleteEvent` / `AllUserCacheDeleteEvent`）集中清理。
- **RabbitMQ**：生产者使用 `correlationId` 链路追踪，消费者手动 `basic.ack` 签收；队列声明后用原生 `queueDeclarePassive` 统计。
- **日志脱敏**：Controller AOP 切面自动对密码、手机号、邮箱、密钥等敏感字段脱敏；存储配置返回时对密钥脱敏。
- **自动装配**：starter 模块通过 `AutoConfiguration.imports` 注册，引入即生效；用 `@ConditionalOnProperty` 控制启停。
- **认证与权限**：Sa-Token 管理会话与权限，`StpInterfaceImpl` 从角色 / 菜单聚合权限写入 Redis。

## 功能模块清单（platform-admin）

| 模块 | 路径前缀 | 主要接口 |
| --- | --- | --- |
| 认证 | `/auth` | login / logout / second-factor/verify |
| 验证码 | `/captcha` | get |
| 用户管理 | `/user` | page / select-list / view / info / add / edit / delete / editStatus / changePassword |
| 角色管理 | `/role` | page / select-list / view / menuIds / enums / add / edit / editStatus / delete / sort / assignMenus |
| 菜单管理 | `/menu` | tree / select-list / view / enums / add / edit / editStatus / delete / sort |
| 部门管理 | `/dept` | page / select-list / view / add / edit / delete / sort |
| 岗位管理 | `/post` | page / select-list / view / enums / add / edit / delete / sort |
| 字典管理 | `/dict` `/dictItem` | 字典 + 字典项（page / view / add / edit / delete） |
| 通知公告 | `/notice` | page / select-list / view / enums / add / edit / delete |
| 参数设置 | `/sysConfig` | page / select-list / view / enums / add / edit / delete |
| 存储配置 | `/storage-config` | page / select-list / view / enums / add / edit / delete / set-default / edit-status |
| 附件 / 分片 | `/attachment` `/attachment/chunk` | upload / page / view / delete / chunk(upload/check/merge) |
| 系统监控 | `/monitor` | server（CPU / 内存 / JVM / 磁盘 / Redis / MQ） |
| 定时任务 | `/job` `/jobLog` | 任务：page / view / add / edit / delete / changeStatus / run / select-list；日志：page / view / delete / clean |
| 操作日志 | `/sysLog` | page / view / delete / clean |
| 登录日志 | `/sysLoginLog` | page / view / delete / clean |

## 接口示例

```bash
# 用户登录（需先取 /captcha/get 验证码）
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456","captchaKey":"xxx","captchaCode":"abcd"}'

# 分页查询用户
curl "http://localhost:8080/user/page?page=1&pageSize=10" \
  -H "Authorization: Bearer <token>"

# 查询用户详情
curl http://localhost:8080/user/view?id=1 \
  -H "Authorization: Bearer <token>"

# 新增用户
curl -X POST http://localhost:8080/user/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"username":"zhangsan","password":"abc123456","email":"zhangsan@example.com","status":1}'

# 服务监控
curl http://localhost:8080/monitor/server \
  -H "Authorization: Bearer <token>"
```

## SQL 脚本

`sql/` 目录存放建表与初始化脚本：

- `rbac.sql`：RBAC 核心表（用户 / 角色 / 菜单 / 部门 / 岗位 / 字典 / 字典项 / 参数 / 日志等）
- `menu_init.sql`：菜单初始化数据（目录 → 页面 → 按钮，按 Controller 接口生成）
- `sys_storage_config.sql` / `sys_attachment.sql`：存储配置与附件表（含本地默认种子）
- 其余业务表脚本按需补充
