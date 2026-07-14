# 项目长期记忆（platform）

## 文件存储 / 附件模块设计约定
- 多存储后端可切换：策略模式 `FileStorage` 接口 + 各实现（Local/Oss/Cos/Minio）+ `FileStorageManager` 按类型路由。
- `sys_storage_config` 表存各后端连接参数，`is_default=1` 决定全局默认存储，后台可热切换无需重启；`status` 控制启用停用。
- `storage_type` 字段用**整型枚举码值**（1本地 2阿里云OSS 3腾讯COS 4MinIO），对应 `StorageTypeEnum`（普通枚举：code + desc + beanName，beanName 绑定对应 `FileStorageFactory` 实现，由 `FileStorageManager` 路由）。不使用字符串。
- `sys_attachment` 表**不冗余** `storage_type`，只存 `config_id` 关联 `sys_storage_config`（类型由关联取得，避免数据不一致）；上传永远指向一条真实配置（无 config_id=0 兜底）。
- 已生成 SQL：`sql/sys_attachment.sql`、`sql/sys_storage_config.sql`（含本地默认配置种子）。
- 代码已落地（已编译通过）：`StorageTypeEnum` + `StorageConfigStatusEnum`；`FileStorage` 接口 + `FileUploadResult` + 4 个实现 + 对应 `FileStorageFactory` + `FileStorageManager`；`SysStorageConfig`/`SysAttachment` 实体+Mapper+Service+Controller；原 `FileService` 删除；`FileProcessComponent` 改为校验后走 `FileStorageManager`；SDK 依赖（aliyun-sdk-oss 3.16.1 / cos_api 5.6.137 / minio 8.5.2）加入 starter + 根 pom dependencyManagement。
- 存储实现分包约定（platform-starter）：共享抽象 `FileStorage`/`FileUploadResult`/`FileStorageFactory`/`FileStorageManager` 留在 `com.platform.starter.file`；四个策略各自独立子包：`file.local`(LocalFileStorage+Factory)、`file.oss`(OssFileStorage+Factory)、`file.cos`(CosFileStorage+Factory)、`file.minio`(MinioFileStorage+Factory)。工厂 `@Component` beanName 保持不变（localFileStorageFactory 等），由 `StorageTypeEnum.getBeanName()` 路由。
