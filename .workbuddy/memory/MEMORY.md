# 项目长期记忆（platform）

## 整体架构总览（7 模块，Spring Boot 3.2.5 / Java 17 / MyBatis-Plus 3.5.5 / Sa-Token 1.45.0）
- 模块依赖链：common(底层,无内部依赖) ← service(依 common) / starter(依 common) ← framework(依 common+service+starter) ← component(依前四者) ← admin(启动模块,依全部)。generator 完全独立。
- **依赖反转点**：framework 反向依赖 service（因 `StpInterfaceImpl` 需调 SysUserService/SysRoleService 加载权限）。
- 各模块职责：
  - common：实体（**实体集中在 common** `entity/` 与 `entity/admin/`）、`result/Result`+`Paging`、`exception/BusinessException`、`enums/ErrorCode`(5位码)、`context/UserContextHolder`(TTL)、常量、annotation(`IgnoreLog`/`JsonCoverParam`)、utils(`Assert`)。
  - service：19 套 Mapper/Service/ServiceImpl(继承 MP `BaseMapper`/`IService`)+XML；`mq/`(UserProducer/UserConsumer 手动ACK)。
  - starter：自动装配"引入即生效"，imports 含 6 个 AutoConfiguration：file/threadpool/ratelimiter/redis/security(BCrypt)/mapperfacade(Orika)。`RedisUtil`、`RateLimit`+切面、文件存储抽象。
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
