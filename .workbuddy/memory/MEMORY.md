# 项目长期记忆（platform）

## 整体架构总览（7 模块，Spring Boot 3.2.5 / Java 17 / MyBatis-Plus 3.5.5 / Sa-Token 1.45.0）
- 模块依赖链：common(底层,无内部依赖) ← service(依 common) / starter(依 common) ← framework(依 common+service+starter) ← component(依前四者) ← admin(启动模块,依全部)。generator 完全独立。
- **依赖反转点**：framework 反向依赖 service（因 `StpInterfaceImpl` 需调 SysUserService/SysRoleService 加载权限）。
- 各模块职责：
  - common：实体（**实体集中在 common** `entity/` 与 `entity/admin/`）、`result/Result`+`Paging`、`exception/BusinessException`、`enums/ErrorCode`(5位码)、`context/UserContextHolder`(TTL)、常量、annotation(`IgnoreLog`/`JsonCoverParam`)、utils(`Assert`)。
  - service：19 套 Mapper/Service/ServiceImpl(继承 MP `BaseMapper`/`IService`)+XML；`mq/`(UserProducer/UserConsumer 手动ACK)。
  - starter：自动装配"引入即生效"，imports 含 7 个 AutoConfiguration：file/threadpool/ratelimiter/redis/security(BCrypt+RSA)/mapperfacade(Orika)。`RedisUtil`、`RateLimit`+切面、文件存储抽象、`RsaComponent`(RSA 2048 密钥对内存生成，私钥不落盘)。
  - framework：全局配置——`MyBatisPlusConfig`(分页+乐观锁)、`SaTokenConfig`、`JacksonConfig`、`RabbitMQConfig`、`SpringDocConfig`、`WebMvcConfig`(仅参数解析器)；**`CorsConfig`(过滤器级 CORS，`FilterRegistrationBean` 设 `Ordered.HIGHEST_PRECEDENCE`，保证预检 OPTIONS 先于 Sa-Token 拦截器拿到 CORS 头)**；`GlobalExceptionHandler`；`security/StpInterfaceImpl`；`interceptor/SaTokenContextInterceptor`；`manager/AsyncManager`；`filter/RequestReplaceFilter`。
  - component：业务组合组件——UserComponent 等(缓存聚合)、CaptchaComponent、PageExportComponent(EasyExcel)、ScheduleComponent(Quartz)、ServerComponent(OSHI监控)、TreeBuilderComponent、分片上传。
  - admin：启动类 `com.platform.PlatformApplication`(@MapperScan com.platform.service.mapper)、全部 Controller、VO、`LogAspect`(操作日志入库+脱敏)、`MySaTokenListener`(登录日志)、application*.yml。
  - generator：MP FastAutoGenerator + Freemarker，Entity→common、Mapper/Service/XML→service，不生成 Controller。
- **已知能力缺口**（待补强定位明确）：① 审计字段自动填充未实现（实体有 createBy/createTime 等字段但无 `MetaObjectHandler` + `@TableField(fill)`）；② 数据权限未实现（无拦截器/切面，`UserContext.tenantId` 已预留）；③ 无 BaseEntity/BaseController/BaseService（实体直接 Serializable，Service 直接继承 MP IService）。

## 文件存储 / 附件模块设计约定
- 多存储后端可切换：策略模式 `FileStorage` 接口 + 各实现（Local/Oss/Cos/Minio）+ `FileStorageManager` 按类型路由。
- `sys_storage_config` 表存各后端连接参数，`is_default=1` 决定全局默认存储，后台可热切换无需重启；`status` 控制启用停用。
- `storage_type` 字段用**整型枚举码值**（1本地 2阿里云OSS 3腾讯COS 4MinIO），对应 `StorageTypeEnum`（普通枚举：code + desc + beanName，beanName 绑定对应 `FileStorageFactory` 实现，由 `FileStorageManager` 路由）。不使用字符串。
- `sys_attachment` 表**不冗余** `storage_type`，只存 `config_id` 关联 `sys_storage_config`（类型由关联取得，避免数据不一致）；上传永远指向一条真实配置（无 config_id=0 兜底）。
- 已生成 SQL：`sql/sys_attachment.sql`、`sql/sys_storage_config.sql`（含本地默认配置种子）。
- 代码已落地（已编译通过）：`StorageTypeEnum` + `StorageConfigStatusEnum`；`FileStorage` 接口 + `FileUploadResult` + 4 个实现 + 对应 `FileStorageFactory` + `FileStorageManager`；`SysStorageConfig`/`SysAttachment` 实体+Mapper+Service+Controller；原 `FileService` 删除；`FileProcessComponent` 改为校验后走 `FileStorageManager`；SDK 依赖（aliyun-sdk-oss 3.16.1 / cos_api 5.6.137 / minio 8.5.2）加入 starter + 根 pom dependencyManagement。
- 存储实现分包约定（platform-starter）：共享抽象 `FileStorage`/`FileUploadResult`/`FileStorageFactory`/`FileStorageManager` 留在 `com.platform.starter.file`；四个策略各自独立子包：`file.local`(LocalFileStorage+Factory)、`file.oss`(OssFileStorage+Factory)、`file.cos`(CosFileStorage+Factory)、`file.minio`(MinioFileStorage+Factory)。工厂 `@Component` beanName 保持不变（localFileStorageFactory 等），由 `StorageTypeEnum.getBeanName()` 路由。

## 后端开发规范（权威来源 = `platform-scaffold-dev` 技能）
- 该技能是代码生成的唯一权威约定，每次新增/修改后端代码前必须遵循。核心约束速记：
  - API 只用 POST/GET；POST=写（add/edit/delete/editStatus/changePassword），GET=查（page/select-list/view/info/enums）。单参 POST 加 `@JsonCoverParam`；VO 参数用 `@Valid @RequestBody`；GET 问号传参不加 `@RequestParam`（id 等可用）。
  - Controller 返回裸 `Result`（不带泛型）；分页直接 `Result.success(paging)`，`Paging extends MP Page`。
  - 每个模块 Controller 必备标准接口：/page /select-list /view /enums /add /edit /delete（可选 /editStatus /sort /changePassword /assignRoles）。
  - VO 隔离：XxxSaveVO/XxxEditVO/XxxVO 独立，**禁止 Entity 做接口入参**；VO 字段校验按表定义（String 加 `@Size(max=列长)`，必填 `@NotBlank`）。
  - 事务只能放 Component 的 `doSomethingInTransactional`（禁止 Controller/Service 直接 `@Transactional`）；查询不进事务。
  - 对象转换用 `MapperFacade`（Orika），**禁止 BeanUtils.copyProperties**。
  - 业务校验在 Controller 层 `checkParams`，Service 只做 CRUD（findById 含存在性校验）。
  - 状态枚举独立（每个实体一套，含 getByCode/getDescByCode/fromStatus）；空值判断用 `Objects.xxx`，禁止 `xx == null`。
  - 列表查询必须 `orderByDesc`；逻辑删除手动 `.eq(is_delete, NORMAL)`（无 `@TableLogic`）。
  - 后置清理（踢下线/清缓存）走 `AsyncManager` + XxxComponent 事件监听，异步执行。
- 与技能模板的细节差异（实际代码现状）：无独立 `PageResult` 类；GET 详情参数用 `@RequestParam @NotNull Long id`（与"不加 @RequestParam"有细微出入，实际以现有写法为准）；MQ 生产端 `UserProducer` 被注释未启用；`RateLimiterAspect` 用调用栈取方法名（脆弱，建议改用 joinPoint）。
