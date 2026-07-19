package com.platform.admin.controller.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.user.*;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.bo.UserInfoBO;
import com.platform.common.constant.RedisConstant;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.*;
import com.platform.common.entity.admin.SysAttachment;
import com.platform.common.enums.*;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.component.admin.config.SysConfigComponent;
import com.platform.component.admin.user.UserComponent;
import com.platform.component.file.AttachmentUrlComponent;
import com.platform.service.service.*;
import com.platform.starter.redis.RedisUtil;
import com.platform.starter.security.RsaComponent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 用户管理控制器
 *
 * <p>提供用户列表查询、用户添加、用户删除、用户详情等接口。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /user/page     - 用户列表（分页查询）</li>
 *   <li>GET  /user/select-list - 用户选择列表（性能查询，仅返回id/昵称/部门）</li>
 *   <li>GET  /user/view     - 用户详情</li>
 *   <li>GET  /user/info     - 用户详情（基本信息 + 角色 + 权限 + 菜单树）</li>
 *   <li>GET  /user/enums    - 用户相关枚举列表（状态）</li>
 *   <li>POST /user/add      - 添加用户</li>
 *   <li>POST /user/edit     - 编辑用户基本信息</li>
 *   <li>POST /user/delete   - 删除用户（逻辑删除）</li>
 *   <li>POST /user/toggleStatus - 切换用户状态（禁用/激活）</li>
 *   <li>POST /user/changePassword - 修改密码</li>
 *   <li>POST /user/assignRoles  - 分配角色</li>
 * </ul>
 *
 * <p>框架规范：</p>
 * <ul>
 *   <li>仅使用 POST 和 GET 两种请求类型：POST 用于数据修改接口，GET 用于查询接口</li>
 *   <li>业务参数校验在 Controller 层执行（checkParams），Service 层只负责数据操作</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "用户管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class SysUserController {

    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;
    private final SysRoleService sysRoleService;
    private final MapperFacade mapperFacade;
    private final PasswordEncoder passwordEncoder;
    private final UserComponent userComponent;
    private final SysConfigComponent sysConfigComponent;
    private final SysDeptService sysDeptService;
    private final SysPostService sysPostService;
    private final RedisUtil redisUtil;
    private final SysAttachmentService sysAttachmentService;
    private final AttachmentUrlComponent attachmentUrlComponent;
    private final RsaComponent rsaComponent;

    // ============================ 查询接口 ============================

    /**
     * 分页查询用户列表
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param status    状态（1正常 0禁用），可选筛选条件
     * @return          用户分页列表
     */
    @Operation(summary = "用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
        Paging<SysUser> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = paramsMap(status, StrUtil.trim(keyword));
        sysUserService.paging(paging, paramsMap);
        // 渲染用户头像
        Set<Long> attIds = paging.getRecords().stream()
                .map(SysUser::getAvatarId)
                .collect(Collectors.toSet());
        Map<Long, SysAttachment> attachmentMap = sysAttachmentService.getMapByIds(attIds);

        paging.convert(user -> {
            UserVO map = mapperFacade.map(user, UserVO.class);
            map.setStatusText(UserStatusEnum.getDescByCode(user.getStatus()));

            String avatarPreviewUrl = attachmentUrlComponent.getPreviewUrl(attachmentMap.get(user.getAvatarId()));
            map.setAvatarPreviewUrl(avatarPreviewUrl);
            return map;
        });
        return Result.success(paging);
    }

    /**
     * 用户选择列表
     *
     * <p>性能接口：仅返回 id、昵称、部门名称三个字段，只查未删除的正常状态用户。
     * 支持 keyword 模糊匹配昵称或部门名称，用于下拉选择、人员指派等场景。</p>
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param keyword   模糊匹配关键词（匹配昵称或部门名称），可选
     * @return          用户选择列表（分页）
     */
    @Operation(summary = "用户选择列表")
    @SaCheckPermission("system:user:select-list")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysUser> paging = new Paging<>(page, pageSize);
        sysUserService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(user -> mapperFacade.map(user, UserSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 用户相关枚举列表
     *
     * <p>返回用户模块前端需要的枚举选项（状态），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "用户相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(UserStatusEnum.values())
                .map(e -> {
                    EnumVO vo = new EnumVO();
                    vo.setCode(e.getCode());
                    vo.setDesc(e.getDesc());
                    return vo;
                }).toList();
        return Result.success(vos);
    }

    /**
     * 用户详情
     *
     * <p>根据用户ID查询用户详细信息（不含密码），返回用户基本信息及状态描述。</p>
     *
     * @param id    用户ID
     * @return      用户详细信息
     */
    @Operation(summary = "用户详情")
    @SaCheckPermission("system:user:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "用户id不能为空") @RequestParam Long id) {
        // findById 包含存在性校验，用户不存在时抛出 BusinessException
        SysUser sysUser = sysUserService.findById(id);
        String avatarPreviewUrl = attachmentUrlComponent.getAccessUrl(sysUser.getAvatarId());

        UserVO vo = mapperFacade.map(sysUser, UserVO.class);
        vo.setStatusText(UserStatusEnum.getDescByCode(sysUser.getStatus()));
        vo.setAvatarPreviewUrl(avatarPreviewUrl);
        if (StrUtil.isNotBlank(sysUser.getPostIds())) {
            vo.setPIds(
                    Arrays.stream(sysUser.getPostIds().split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet())
            );
        }
        if (StrUtil.isNotBlank(sysUser.getRoleIds())) {
            vo.setRIds(
                    Arrays.stream(sysUser.getRoleIds().split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet())
            );
        }
        return Result.success(vo);
    }

    /**
     * 用户详情（聚合：基本信息 + 角色 + 权限 + 菜单树）
     *
     * <p>数据聚合与缓存读写统一由 {@link UserComponent#getUserInfo} 负责：优先读缓存，
     * 未命中则查库聚合（基本信息、角色、角色关联菜单推导的权限）后写回缓存。
     * 用户角色/权限/状态变更时由 {@link UserComponent#cleanUserCache} /
     * {@link UserComponent#cleanUserCacheAndSession} 异步失效缓存。</p>
     *
     * <p>本方法只负责将聚合数据转换为对外展示的 VO（含状态文本、id 集合拆分、
     * 菜单树构建）——该转换依赖 admin 层 VO，无法下沉到 component 模块。</p>
     *
     * @return      用户聚合详情
     */
    @Operation(summary = "用户详情（角色+权限+菜单）")
    @GetMapping("/info")
    public Result info() {
        Long id = SecurityUser.getUserId();
        // 数据聚合 + 缓存 + 菜单树构建统一由组件层处理，此处仅做 BO → VO 的展示层转换
        UserInfoBO bo = userComponent.getUserInfo(id);
        UserInfoVO vo = mapperFacade.map(bo, UserInfoVO.class);
        return Result.success(vo);
    }

    // ============================ 数据修改接口 ============================

    /**
     * 添加用户
     *
     * <p>校验业务参数（性别、状态合法性）及用户名是否重复后，密码使用 BCrypt 加密存储。</p>
     *
     * @param saveVO    用户添加入参
     * @return          操作结果
     */
    @Operation(summary = "添加用户")
    @SaCheckPermission("system:user:add")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody UserSaveVO saveVO) {
        checkUserSaveVO(saveVO);
        SysUser sysUser = mapperFacade.map(saveVO, SysUser.class);
        String initPassword = sysConfigComponent.getConfig(SysConfigKeyEnum.USER_INIT_PASSWORD.getConfigKey());
        sysUser.setPassword(passwordEncoder.encode(initPassword))
                .setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        // 需要更新一下用户的头像附件的关联id
        SysAttachment attachment = sysAttachmentService.findById(saveVO.getAvatarId());

        userComponent.doSomethingInTransactional(() -> {
            sysUserService.save(sysUser);
            // 如果有角色
            if (StrUtil.isNotBlank(sysUser.getRoleIds())) {
                List<SysUserRole> userRoleList = saveVO.getRIds().stream()
                        .map(rid -> {
                            return new SysUserRole()
                                    .setUserId(sysUser.getId())
                                    .setRoleId(rid);
                        }).toList();
                sysUserRoleService.saveBatch(userRoleList);
            }
            sysAttachmentService.lambdaUpdate()
                    .eq(SysAttachment::getId, attachment.getId())
                    .set(SysAttachment::getBizType, AttachmentBizTypeEnum.AVATAR.getCode())
                    .set(SysAttachment::getBizId, sysUser.getId())
                    .set(SysAttachment::getUpdateBy, SecurityUser.getUserId())
                    .set(SysAttachment::getUpdateTime, LocalDateTime.now())
                    .update();
            return true;
        });
        return Result.success();
    }

    private void checkUserSaveVO(UserSaveVO saveVO) {
        Integer gender = saveVO.getGender();
        Assert.notNull(GenderEnum.getByCode(gender), "性别值不合法（0未知 1男 2女）");
        checkUsernameNotExist(saveVO.getUsername());
        SysDept sysDept = sysDeptService.findById(saveVO.getDeptId());
        Assert.notNull(sysDept, "无效的部门id：{}", saveVO.getDeptId());
        saveVO.setDeptName(sysDept.getDeptName());
        // 校验岗位
        if (CollUtil.isNotEmpty(saveVO.getPIds())) {
            Set<Long> pIds = saveVO.getPIds();
            Map<Long, SysPost> postMap = sysPostService.findPartMapByIds(pIds);
            pIds.forEach(id -> {
                Assert.notNull(postMap.get(id), "无效的岗位id：{}", id);
            });
            String postIds = pIds.stream().map(Object::toString).collect(Collectors.joining(","));
            saveVO.setPostIds(postIds);
        }
        // 校验角色
        if (CollUtil.isNotEmpty(saveVO.getRIds())) {
            Set<Long> rIds = saveVO.getRIds();
            Map<Long, SysRole> roleMap = sysRoleService.findPartMap(rIds);
            rIds.forEach(id -> {
                Assert.notNull(roleMap.get(id), "无效的角色id：{}", id);
            });
            String roleIds = rIds.stream().map(Object::toString).collect(Collectors.joining(","));
            saveVO.setRoleIds(roleIds);
        }
    }

    /**
     * 编辑用户基本信息
     *
     * <p>仅修改用户基本信息（昵称、头像、性别、出生日期、部门、岗位、角色、备注），
     * 不涉及手机号、邮箱、密码、状态的修改。性别字段需校验合法性。</p>
     *
     * @param editVO    用户编辑入参
     * @return          操作结果
     */
    @Operation(summary = "编辑用户基本信息")
    @SaCheckPermission("system:user:edit")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody UserEditVO editVO) {
        checkUserEditVO(editVO);
        sysUserService.findById(editVO.getId());
        SysAttachment attachment = sysAttachmentService.findById(editVO.getAvatarId());

        SysUser sysUser = mapperFacade.map(editVO, SysUser.class);
        sysUser.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        userComponent.doSomethingInTransactional(() -> {
            sysUserService.updateById(sysUser);
            // 删除用户原有的角色关联
            sysUserRoleService.lambdaUpdate()
                    .eq(SysUserRole::getUserId, editVO.getId())
                    .remove();
            // 如果有角色
            if (StrUtil.isNotBlank(sysUser.getRoleIds())) {
                List<SysUserRole> userRoleList = editVO.getRIds().stream()
                        .map(rid -> {
                            return new SysUserRole()
                                    .setUserId(sysUser.getId())
                                    .setRoleId(rid);
                        }).toList();
                sysUserRoleService.saveBatch(userRoleList);
            }
            sysAttachmentService.lambdaUpdate()
                    .eq(SysAttachment::getId, attachment.getId())
                    .set(SysAttachment::getBizType, AttachmentBizTypeEnum.AVATAR.getCode())
                    .set(SysAttachment::getBizId, sysUser.getId())
                    .set(SysAttachment::getUpdateBy, SecurityUser.getUserId())
                    .set(SysAttachment::getUpdateTime, LocalDateTime.now())
                    .update();
            return true;
        });
        // 异步清除用户权限/角色缓存（不踢下线）
        userComponent.cleanUserCache(sysUser.getId());
        return Result.success();
    }

    private void checkUserEditVO(UserEditVO editVO) {
        Integer gender = editVO.getGender();
        Assert.notNull(GenderEnum.getByCode(gender), "性别值不合法（0未知 1男 2女）");
        SysDept sysDept = sysDeptService.findById(editVO.getDeptId());
        Assert.notNull(sysDept, "无效的部门id：{}", editVO.getDeptId());
        editVO.setDeptName(sysDept.getDeptName());
        // 校验岗位
        if (CollUtil.isNotEmpty(editVO.getPIds())) {
            Set<Long> pIds = editVO.getPIds();
            Map<Long, SysPost> postMap = sysPostService.findPartMapByIds(pIds);
            pIds.forEach(id -> {
                Assert.notNull(postMap.get(id), "无效的岗位id：{}", id);
            });
            String postIds = pIds.stream().map(Object::toString).collect(Collectors.joining(","));
            editVO.setPostIds(postIds);
        }
        // 校验角色
        if (CollUtil.isNotEmpty(editVO.getRIds())) {
            Set<Long> rIds = editVO.getRIds();
            Map<Long, SysRole> roleMap = sysRoleService.findPartMap(rIds);
            rIds.forEach(id -> {
                Assert.notNull(roleMap.get(id), "无效的角色id：{}", id);
            });
            String roleIds = rIds.stream().map(Object::toString).collect(Collectors.joining(","));
            editVO.setRoleIds(roleIds);
        }
    }

    /**
     * 删除用户（逻辑删除）
     *
     * <p>校验用户存在性后，将用户标记为已删除（is_delete=1），不物理删除记录。
     * 逻辑删除后，异步执行后置清理（踢下线 + 清缓存），复用 {@link UserComponent#cleanUserCacheAndSession}。</p>
     *
     * @param id    用户ID
     * @return      操作结果
     */
    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的用户") Long id) {
        // 1. 校验用户是否存在（findById 包含存在性校验）
        sysUserService.findById(id);
        // 2. 逻辑删除（同步，保证返回前数据已落库）
        sysUserService.lambdaUpdate()
                .eq(SysUser::getId, id)
                .set(SysUser::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysUser::getUpdateBy, SecurityUser.getUserId())
                .set(SysUser::getUpdateTime, LocalDateTime.now())
                .update();
        // 3. 异步后置清理：踢下线 + 清缓存（删除/禁用用户复用同一逻辑）
        userComponent.cleanUserCacheAndSession(id);
        return Result.success();
    }

    /**
     * 切换用户状态
     *
     * <p>根据用户当前状态取反：正常→禁用，禁用→正常。
     * 切换为禁用时异步执行后置清理（踢下线 + 清缓存），复用 {@link UserComponent#cleanUserCacheAndSession}，
     * 防止已禁用用户继续操作。</p>
     *
     * @param id    用户ID
     * @return      操作结果
     */
    @Operation(summary = "切换用户状态")
    @SaCheckPermission("system:user:editStatus")
    @PostMapping("/editStatus")
    @JsonCoverParam
    public Result editStatus(@NotNull(message = "请选择需要操作的用户") Long id) {
        // 1. 校验用户是否存在（findById 包含存在性校验）
        SysUser sysUser = sysUserService.findById(id);
        // 2. 根据当前状态取反
        Integer targetStatus = UserStatusEnum.NORMAL.fromStatus(sysUser.getStatus())
                ? UserStatusEnum.DISABLED.getCode()
                : UserStatusEnum.NORMAL.getCode();
        sysUserService.lambdaUpdate()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, targetStatus)
                .set(SysUser::getUpdateBy, SecurityUser.getUserId())
                .set(SysUser::getUpdateTime, LocalDateTime.now())
                .update();
        // 3. 切换为禁用时，异步清理会话与缓存；激活时无需清理
        userComponent.cleanUserCacheAndSession(id);
        return Result.success();
    }

    /**
     * 修改密码
     *
     * <p>当前登录用户修改自己的密码：校验旧密码是否正确、新密码与确认密码是否一致，
     * 校验通过后新密码使用 BCrypt 加密存储。</p>
     *
     * <p>用户ID从登录态获取，无需前端传入。旧密码校验复用 {@link SysUserService#findById}，
     * 该方法是通用的"根据ID查用户"方法，后续编码中可反复复用，无需为密码修改单独写查询。</p>
     *
     * @param passwordVO    修改密码入参（旧密码、新密码、确认新密码）
     * @return              操作结果
     */
    @Operation(summary = "修改密码")
    @SaCheckPermission("system:user:changePassword")
    @PostMapping("/changePassword")
    public Result changePassword(@Valid @RequestBody UserPasswordVO passwordVO) {
        // 修改密码需要二次密码认证
        if (!redisUtil.hasKey(RedisConstant.SECOND_AUTH_LOCK + SecurityUser.getUserId())) {
            throw new BusinessException(ErrorCode.SECOND_AUTH);
        }
        // 前端必须用 RSA 公钥加密，此处无条件解密（PKCS1 随机填充，两次密文不同，须在解密后比对）
        String oldPassword = rsaComponent.decryptByPrivateKey(passwordVO.getOldPassword());
        String newPassword = rsaComponent.decryptByPrivateKey(passwordVO.getNewPassword());
        String confirmPassword = rsaComponent.decryptByPrivateKey(passwordVO.getConfirmPassword());
        // 1. 校验新密码与确认密码是否一致（比对解密后的明文）
        Assert.isTrue(Objects.equals(newPassword, confirmPassword), "两次输入的密码不一致");
        // 1.1 校验新密码长度（密文已解密，恢复明文长度约束）
        Assert.isTrue(newPassword.length() >= 6 && newPassword.length() <= 64, "密码长度必须在6~64个字符之间");
        // 2. 获取当前登录用户（复用 findById，返回的 User 包含 password 字段，无需单独查询）
        SysUser sysUser = sysUserService.findById(passwordVO.getId());
        // 3. 校验旧密码是否正确
        Assert.isTrue(passwordEncoder.matches(oldPassword, sysUser.getPassword()), () -> new BusinessException(ErrorCode.USER_PASSWORD_ERROR));
        // 4. 更新密码（BCrypt加密）
        sysUserService.lambdaUpdate()
                .eq(SysUser::getId, passwordVO.getId())
                .set(SysUser::getPassword, passwordEncoder.encode(newPassword))
                .set(SysUser::getUpdateBy, SecurityUser.getUserId())
                .set(SysUser::getUpdateTime, LocalDateTime.now())
                .update();
        return Result.success();
    }

    // ============================ 私有校验方法 ============================

    /**
     * 校验用户名是否已存在
     *
     * <p>查询未删除的用户中是否已存在相同用户名，存在则抛出 {@link BusinessException}。</p>
     *
     * @param username  用户名
     */
    private void checkUsernameNotExist(String username) {
        SysUser existSysUser = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.isNull(existSysUser, () -> new BusinessException(ErrorCode.USER_NAME_EXIST));
    }

    /**
     * 构建查询参数Map
     *
     * @param status    状态筛选条件
     * @return          参数Map
     */
    private Map<String, Object> paramsMap(Integer status, String keyword) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("keyword", keyword);
        return paramsMap;
    }
}
