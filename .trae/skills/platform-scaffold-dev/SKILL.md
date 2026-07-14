---
name: "platform-scaffold-dev"
description: "Platform scaffold development guide for Spring Boot 3.2.5 multi-module platform. Invoke when generating Controller/Service/Entity/Mapper code, adding API endpoints, or any development task in this platform."
---

# Platform Scaffold Development Guide

## 项目技术栈

- Spring Boot 3.2.5 + Java 17
- MyBatis-Plus 3.5.5 + Sa-Token 1.45.0 + SpringDoc OpenAPI 2.3.0
- Redis(Lettuce) + RabbitMQ + Orika + BCrypt
- JDK: D:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
- Maven: D:\Program Files\apache-maven-3.9.16\bin\mvn.cmd，本地仓库: D:\Program Files\maven
- 编译命令: mvn compile -pl platform-admin -am -DskipTests -q

## 模块结构与依赖方向

| 模块                | 职责 | 依赖方向 |
|-------------------|------|----------|
| platform-common   | Entity、Enum、Constant、Exception、Utils、Annotation | 被所有模块依赖 |
| platform-starter   | 自动配置类（Redis、ThreadPool、PasswordEncoder、MapperFacade） | -> common |
| platform-framework | 框架配置、Handler、Filter、Manager | -> starter -> common |
| platform-service   | Service接口/实现、Mapper接口、Mapper XML | -> common |
| platform-component | 业务组合组件（跨基础设施复用逻辑） | -> framework -> starter -> common |
| platform-admin     | Controller、VO、启动入口（PlatformApplication） | -> framework, service, component, common |

## 包路径规范（严格遵循）

| 文件类型 | 完整包路径 |
|----------|-----------|
| Entity | com.platform.common.entity |
| Enum | com.platform.common.enums |
| Constant | com.platform.common.constant |
| Annotation | com.platform.common.annotation |
| Exception | com.platform.common.exception |
| Utils | com.platform.common.utils |
| Context | com.platform.common.context |
| Config | com.platform.framework.config |
| Manager | com.platform.framework.manager |
| Handler | com.platform.framework.handler |
| Service接口 | com.platform.service.service |
| Service实现 | com.platform.service.service.impl |
| Mapper接口 | com.platform.service.mapper |
| Mapper XML | platform-service/src/main/resources/mapper |
| Controller | com.platform.admin.controller |
| VO | com.platform.admin.vo |
| Component | com.platform.component.admin.{module} |

## API 设计规范

1. **只使用 POST 和 GET 两种请求类型**
   - POST 用于数据修改接口（add, delete, update, changePassword, toggleStatus）
   - GET 用于查询接口（page, view, info, enums）
2. **单参数 POST** 使用 `@JsonCoverParam` 注解，参数作为 JSON body 传输
3. **VO 参数 POST** 使用 `@Valid @RequestBody`
4. **GET 查询参数走问号传参**：直接声明参数名，不加 `@RequestParam` 注解（如 `public Result view(Long id)`）
5. **所有接口必须写注释**：类级 `@Tag` + 方法级 `@Operation` + Javadoc
6. **接口/方法名使用动词语义**：add, delete, view, toggleStatus, changePassword, page
7. **Controller 层返回类型统一使用裸 `Result`**，不指定泛型参数
   - 正确：`public Result login(LoginVO vo)`、`public Result page(...)`
   - 错误：`public Result<CaptchaVO> generate()`

## 标准 Controller 接口模板（每个模块必须具备）

每个模块的 Controller 至少包含以下接口：

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | /page | 分页查询，支持keyword模糊匹配 |

## page 接口规范

- 路径：`GET /{module}/page`
- 参数：`page`、`pageSize`、`status`（可选）、`keyword`（可选，模糊匹配名称）
- **Controller 层用 `StrUtil.trim(keyword)` 去空格**后放入 `paramsMap` 传入 Service
- Service 层从 `paramsMap` 取 keyword，用 `lambdaQuery().like()` 或 `.and(w -> w.like().or().like())` 模糊匹配
- 走 Mapper XML 的查询在 XML 中用 `<if test="paramsMap.keyword != null and paramsMap.keyword != ''">` 判断
- keyword 为 null 或空白时自动跳过模糊匹配
- 模糊匹配字段参考：
  - User：nickname、dept_name（两个字段 OR 匹配）
  - Role：role_name、role_code（两个字段 OR 匹配）
  - Menu：menu_name（单字段匹配）
- 示例（lambdaQuery 方式）：
  ```java
  // Controller
  public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
      Paging<Role> paging = new Paging<>(page, pageSize);
      Map<String, Object> paramsMap = new HashMap<>();
      paramsMap.put("status", status);
      paramsMap.put("keyword", StrUtil.trim(keyword));
      roleService.paging(paging, paramsMap);
      // ...
  }

  // ServiceImpl
  public void paging(Paging<Role> paging, Map<String, Object> paramsMap) {
      String keyword = (String) paramsMap.get("keyword");
      lambdaQuery()
              .eq(Role::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
              .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                      w -> w.like(Role::getRoleName, keyword)
                              .or()
                              .like(Role::getRoleCode, keyword))
              .orderByDesc(Role::getId)
              .page(paging);
  }
  ```
- 示例（Mapper XML 方式）：
  ```xml
  <if test="paramsMap.keyword != null and paramsMap.keyword != ''">
      AND (u.nickname LIKE CONCAT('%', #{paramsMap.keyword}, '%')
           OR u.dept_name LIKE CONCAT('%', #{paramsMap.keyword}, '%'))
  </if>
  ```
| 选择列表 | GET | /select-list | 性能查询，仅返回id+名称，支持keyword模糊匹配 |

## select-list 接口规范

- 路径：`GET /{module}/select-list`
- 参数：`page`、`pageSize`、`keyword`（可选，模糊匹配名称）
- **Controller 层用 `StrUtil.trim(keyword)` 去空格**后传入 Service
- Service 层用 `lambdaQuery().like(条件, 字段, keyword)` 模糊匹配，keyword 为 null 或空白时自动跳过
- 只查性能字段（id + name），只返回未删除的正常状态数据，`orderByDesc` 倒序
- 示例：
  ```java
  // Controller
  public Result selectList(Integer page, Integer pageSize, String keyword) {
      Paging<Role> paging = new Paging<>(page, pageSize);
      roleService.selectList(paging, StrUtil.trim(keyword));
      paging.convert(sysRole -> mapperFacade.map(sysRole, RoleSelectVO.class));
      return Result.success(paging);
  }

  // Service
  public void selectList(Paging<Role> paging, String keyword) {
      lambdaQuery()
              .select(Role::getId, Role::getRoleName)
              .eq(Role::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
              .eq(Role::getStatus, RoleStatusEnum.NORMAL.getCode())
              .like(Objects.nonNull(keyword) && !keyword.isBlank(), Role::getRoleName, keyword)
              .orderByDesc(Role::getId)
              .page(paging);
  }
  ```
  
| 详情 | GET | /view | 按ID查询详情 |
| 枚举列表 | GET | /enums | 返回该模块关联的枚举选项（如有枚举） |
| 添加 | POST | /add | 新增数据 |
| 编辑 | POST | /edit | 修改数据 |
| 删除 | POST | /delete | 逻辑删除 |

可选接口（按需添加）：
- `/editStatus` - 切换状态（实体有 status 字段时）
- `/changePassword` - 修改密码（用户模块专用）
- `/assignRoles` - 分配角色（用户模块专用）

## 枚举列表接口规范

- 路径：`GET /{module}/enums`
- 直接返回 `List<EnumVO>`，不使用 Map 包装
- `EnumVO` 位于 `com.platform.common.vo`，含 code 和 desc 两个字段
- 在 Controller 内用 `Arrays.stream` 构造，不使用枚举类的 `toList()` 方法
- 示例：
  ```java
  @GetMapping("/enums")
  public Result enums() {
      List<EnumVO> vos = Arrays.stream(RoleStatusEnum.values())
              .map(e -> {
                  EnumVO vo = new EnumVO();
                  vo.setCode(e.getCode());
                  vo.setDesc(e.getDesc());
                  return vo;
              }).toList();
      return Result.success(vos);
  }
  ```

## 分层校验规范

- **Controller 层**：协议级格式校验（`@Valid`, `@NotNull`, `@Size`）+ 业务参数校验（`checkParams` 等私有方法）
- **Service 层**：只负责数据操作（CRUD），不做业务校验（`findById` 例外，包含存在性校验）
- 字段空值判断使用 `Objects` 方法（`Objects.nonNull`, `Objects.equals`）
- 值校验使用枚举，禁止魔法值；没有枚举就生成
- `Assert` 工具类用于业务断言

## VO 规范

- **每个模块的增删改查使用独立的 VO**：`XxxSaveVO`（添加）、`XxxEditVO`（编辑）、`XxxVO`（展示），禁止直接用 Entity 作为接口入参
- **VO 字段校验必须依据 SQL 表定义**：
  - String 类型字段：`@NotBlank`（必填）+ `@Size(max=列长度)` 校验
  - Integer/Long 类型字段：`@NotNull`（必填）
  - 可选字段：仅 `@Size(max=列长度)` 不加 `@NotBlank`
- **示例**（sysRole 表 role_name VARCHAR(64)）：
  ```java
  @NotBlank(message = "角色名称不能为空")
  @Size(max = 64, message = "角色名称长度不能超过64个字符")
  private String roleName;
  ```

## 枚举规范

- **每个实体有独立的状态枚举**，禁止跨实体复用状态枚举
  - 用户状态：`UserStatusEnum`（0禁用 1正常）
  - 角色状态：`RoleStatusEnum`（0禁用 1正常）
  - 菜单状态：`MenuStatusEnum`（0禁用 1正常）
  - 菜单类型：`MenuTypeEnum`（1目录 2菜单 3按钮）
  - 删除状态：`DeleteStatusEnum`（0未删除 1已删除）
  - 性别：`GenderEnum`（0未知 1男 2女）
- 枚举必须提供：`getByCode`、`getDescByCode`、`fromStatus` 方法

## 查询排序规范

- **所有列表查询接口的 SQL 必须倒序排序**（`orderByDesc`）
- 默认按主键 `id` 倒序：`.orderByDesc(Xxx::getId)`
- 如有特殊排序字段（如 `display_order`），按业务需求倒序

## 事务规范

- **涉及多条 SQL 写操作（INSERT/UPDATE/DELETE）必须使用事务**
- 每个 Component 默认提供 `doSomethingInTransactional` 方法：
  ```java
  @Transactional(rollbackFor = Exception.class)
  public <T> T doSomethingInTransactional(Supplier<T> supplier) {
      return supplier.get();
  }
  ```
- **事务使用位置判断**：
  - 逻辑**不能复用** → 直接在 Controller 中调用 `component.doSomethingInTransactional(() -> { ... })`
  - 逻辑**能复用** → 封装到对应的 Component 方法中，方法上加 `@Transactional`
- 查询操作不需要事务
- 示例（Controller 中直接使用）：
  ```java
  userComponent.doSomethingInTransactional(() -> {
      userRoleService.lambdaUpdate().eq(UserRole::getUserId, userId).remove();
      userRoleService.saveBatch(sysUserRoles);
      return null;
  });
  ```

## Controller 代码模板

```java
@Tag(name = "模块名")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/module")
public class XxxController {

    private final XxxService xxxService;
    private final MapperFacade mapperFacade;
    private final UserComponent userComponent;

    // GET 查询接口
    @Operation(summary = "列表查询")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status) {
        // ...
    }

    // GET 详情接口
    @Operation(summary = "详情")
    @GetMapping("/view")
    public Result view(@NotNull(message = "ID不能为空") @RequestParam Long id) {
        Xxx entity = xxxService.findById(id);
        XxxVO vo = mapperFacade.map(entity, XxxVO.class);
        return Result.success(vo);
    }

    // POST 添加接口（VO参数）
    @Operation(summary = "添加")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody XxxSaveVO saveVO) {
        checkParams(saveVO.getField1(), saveVO.getField2());
        Xxx entity = mapperFacade.map(saveVO, Xxx.class);
        xxxService.save(entity);
        return Result.success();
    }

    // POST 删除接口（单参数）
    @Operation(summary = "删除")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的记录") Long id) {
        xxxService.findById(id);
        xxxService.lambdaUpdate()
                .set(Xxx::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .eq(Xxx::getId, id)
                .update();
        return Result.success();
    }

    // 私有校验方法
    private void checkParams(Integer field1, Integer status) {
        if (Objects.nonNull(field1)) {
            Assert.notNull(SomeEnum.getByCode(field1), "field1值不合法");
        }
        Assert.notNull(StatusEnum.getByCode(status), "状态值不合法");
    }
}
```

## VO 代码模板

```java
@Data
@Schema(description = "描述")
public class XxxVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字段描述")
    @NotBlank(message = "字段不能为空")
    private String field;
}
```

## 枚举代码模板

```java
public enum XxxEnum {

    VALUE1(1, "描述1"),
    VALUE2(2, "描述2");

    private final Integer code;
    private final String desc;

    XxxEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }

    public static XxxEnum getByCode(Integer code) {
        if (Objects.isNull(code)) return null;
        for (XxxEnum value : values()) {
            if (value.code.equals(code)) return value;
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code)).map(XxxEnum::getDesc).orElse("");
    }

    public boolean fromCode(Integer code) { return this.code.equals(code); }
}
```

## 组件层模式

- `platform-component` 放置跨基础设施的复用业务逻辑
- 每个业务模块有独立的 Component，封装该模块的后置清理逻辑
- 典型：
  - `UserComponent.cleanUserCacheAndSession(userId)` — 封装"踢下线 + 清缓存"，删除/禁用用户复用
  - `RoleComponent.cleanRoleCache()` — 封装"清除所有用户权限/角色缓存"，禁用/删除角色复用
  - `MenuComponent.cleanMenuCache()` — 封装"清除所有用户权限/角色缓存"，禁用/删除菜单复用
- Component 组合 AsyncManager、RedisUtil、Sa-Token 等完成业务动作
- Controller 调用 Component 处理后置清理，不直接接触 Sa-Token/Redis
- 状态切换接口（editStatus）模式：根据当前状态取反，仅在切换为禁用时触发缓存清理

## 异步任务模式

- `AsyncManager` 位于 platform-framework，封装 platformAsyncExecutor 线程池
- `asyncManager.execute(Runnable)` 为 fire-and-forget，内部自动捕获异常并记录日志
- 数据操作必须同步执行，后置清理（踢下线、清缓存）异步执行

## Redis 规范

- 所有 key 自动加前缀 `platform:`（通过 RedisUtil）
- 缓存 key 定义在 `RedisConstant` 常量类
- 用户权限缓存：`RedisConstant.USER_AUTH_PERM + userId`
- 用户角色缓存：`RedisConstant.USER_AUTH_ROLE + userId`
- 无显式 TTL 使用全局默认（30分钟）

## Sa-Token 规范

- `SecurityUser.getUserId()` 获取当前登录用户ID
- `StpUtil.isLogin(id)` 判断用户是否在线
- `StpUtil.kickout(id)` 踢用户下线

## 密码规范

- 使用 `BCryptPasswordEncoder`（spring-security-crypto）
- 加密：`passwordEncoder.encode(rawPassword)`
- 校验：`passwordEncoder.matches(rawPassword, encodedPassword)`

## 对象转换规范

- 使用 Orika `MapperFacade`
- `mapperFacade.map(source, TargetClass.class)`
- 禁止使用 `BeanUtils.copyProperties`

## MyBatis-Plus 规范

- 使用 `lambdaQuery()` / `lambdaUpdate()` 进行类型安全查询
- 逻辑删除：`is_delete` 字段（0=正常, 1=已删除），手动处理
- `findById` 包含 `is_delete=0` 条件，不存在时抛 `BusinessException`

## Swagger/OpenAPI 规范

- 分组配置使用编程式 `GroupedOpenApi` Bean（非 YAML group-configs）
- 通过 `.addOperationCustomizer()` 绑定 `OpenApiOperationCustomizer`
- 编译器配置必须包含 `<parameters>true</parameters>`

## 注释规范

- 所有类、方法、字段必须写中文注释
- 类注释包含：功能描述、使用方式、@author platform
- 方法注释包含：功能描述、参数说明、返回值说明
- 遵循阿里巴巴Java开发手册

## 编码规范清单（生成代码时逐条检查）

1. [ ] 文件生成在正确的模块和包路径下
2. [ ] Controller 只用 POST/GET，方法名用动词语义，返回类型用裸 `Result`（不带泛型）
3. [ ] Controller 必须具备标准接口：/page /select-list /view /enums /add /edit /delete
4. [ ] 单参数 POST 用 @JsonCoverParam，VO 参数用 @Valid @RequestBody
5. [ ] GET 请求走问号传参，不加 @RequestParam 注解
6. [ ] 业务校验在 Controller 层（checkParams），Service 只做数据操作
7. [ ] 字段空值判断用 Objects 方法，禁止 xx != null / xx == null
8. [ ] 值校验用枚举，无魔法值；每个实体有独立的状态枚举
9. [ ] Controller 提供 /enums 接口，用 Arrays.stream 构造 List<EnumVO>
10. [ ] 增删改查用独立 VO（SaveVO/EditVO/VO），禁止用 Entity 做入参
11. [ ] VO 字段校验依据 SQL 表定义：String 加 @Size(max=列长度)
12. [ ] 所有类/方法/字段写中文注释
13. [ ] 对象转换用 MapperFacade，不用 BeanUtils
14. [ ] 查询用 lambdaQuery/lambdaUpdate
15. [ ] 列表查询必须 orderByDesc 倒序排序
16. [ ] 多条 SQL 写操作必须用事务（doSomethingInTransactional）
17. [ ] 后置清理用 AsyncManager + Component（UserComponent/RoleComponent）
