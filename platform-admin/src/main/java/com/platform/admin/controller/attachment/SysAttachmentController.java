package com.platform.admin.controller.attachment;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.attachment.AttachmentVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysAttachment;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.AttachmentBizTypeEnum;
import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.service.service.SysAttachmentService;
import com.platform.service.service.SysStorageConfigService;
import com.platform.component.file.AttachmentUrlComponent;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileStorageManager;
import com.platform.starter.file.FileUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 附件管理控制器
 *
 * <p>提供文件上传（走 DB 中的存储配置，默认走 is_default=1 的配置，也可显式指定 configId）、
 * 分页列表、详情、删除（同步删除物理文件）等接口。上传落库 {@code sys_attachment}。</p>
 *
 * <ul>
 *   <li>POST /attachment/upload - 文件上传（落库并返回附件信息）</li>
 *   <li>GET  /attachment/page  - 附件列表（分页，可按 configId/bizType/文件名筛选）</li>
 *   <li>GET  /attachment/view  - 附件详情</li>
 *   <li>POST /attachment/delete - 删除附件（同步删除物理文件）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "附件管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/attachment")
public class SysAttachmentController {

    private final FileStorageManager fileStorageManager;
    private final SysStorageConfigService storageConfigService;
    private final SysAttachmentService attachmentService;
    private final MapperFacade mapperFacade;
    private final AttachmentUrlComponent attachmentUrlComponent;

    /**
     * 文件上传
     *
     * @param file     上传文件
     * @param configId 指定存储配置ID（为空则走默认存储）
     * @param bizType  业务类型 code（见 AttachmentBizTypeEnum：1头像 2文章图片 3文档 4导入模板 5其他）
     * @param bizId    业务ID（如用户ID）
     * @return 附件信息
     */
    @Operation(summary = "文件上传")
    @SaCheckPermission("system:file:upload")
    @PostMapping("/upload")
    public Result upload(@NotNull(message = "上传文件不能为空") @RequestParam("file") MultipartFile file,
                         Long configId,
                         Integer bizType,
                         Long bizId) {
        Assert.isTrue(!file.isEmpty(), "上传文件不能为空");
        SysStorageConfig config = (configId != null)
                ? storageConfigService.findById(configId)
                : storageConfigService.findDefault();
        FileStorage storage = fileStorageManager.getStorage(config);
        FileUploadResult result = storage.upload(file);

        SysAttachment attach = new SysAttachment();
        attach.setConfigId(config.getId());
        attach.setFileName(Objects.toString(file.getOriginalFilename(), "unknown"));
        attach.setFileKey(result.getFileKey());
        attach.setFileExt(parseExt(file.getOriginalFilename()));
        attach.setContentType(file.getContentType());
        attach.setFileSize(file.getSize());
        attach.setBizType(bizType);
        attach.setBizId(bizId);
        attach.setCreateBy(SecurityUser.getUserId());
        attach.setCreateTime(LocalDateTime.now());
        attach.setUpdateBy(SecurityUser.getUserId());
        attach.setUpdateTime(LocalDateTime.now());
        attachmentService.save(attach);

        AttachmentVO vo = mapperFacade.map(attach, AttachmentVO.class);
        vo.setBizTypeDesc(AttachmentBizTypeEnum.getDescByCode(attach.getBizType()));
        fillUrls(attach, vo);
        return Result.success(vo);
    }

    /**
     * 附件分页列表
     */
    @Operation(summary = "附件列表")
    @SaCheckPermission("system:file:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Long configId, Integer bizType, String keyword) {
        Paging<SysAttachment> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>(8);
        paramsMap.put("configId", configId);
        paramsMap.put("bizType", bizType);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        attachmentService.paging(paging, paramsMap);
        // 取本页所有 configId 的 set，批量查部分字段（id + configName），避免逐条查/N+1
        Set<Long> configIds = paging.getRecords().stream()
                .map(SysAttachment::getConfigId)
                .collect(Collectors.toSet());
        Map<Long, String> configNameMap = storageConfigService.mapConfigNameByIds(configIds);
        paging.convert(att -> {
            AttachmentVO vo = mapperFacade.map(att, AttachmentVO.class);
            vo.setConfigName(configNameMap.get(att.getConfigId()));
            vo.setBizTypeDesc(AttachmentBizTypeEnum.getDescByCode(att.getBizType()));
            fillUrls(att, vo);
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 附件详情
     */
    @Operation(summary = "附件详情")
    @SaCheckPermission("system:file:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "附件ID不能为空") Long id) {
        SysAttachment att = attachmentService.findById(id);
        AttachmentVO vo = mapperFacade.map(att, AttachmentVO.class);
        vo.setBizTypeDesc(AttachmentBizTypeEnum.getDescByCode(att.getBizType()));
        fillUrls(att, vo);
        return Result.success(vo);
    }

    /**
     * 附件下载（强制下载：Content-Disposition: attachment）
     */
    @Operation(summary = "附件下载")
    @SaCheckPermission("system:file:download")
    @GetMapping("/download")
    public void download(@NotNull(message = "附件ID不能为空") Long id, HttpServletResponse response) {
        doServe(id, response, true);
    }

    /**
     * 附件预览（浏览器内联：Content-Disposition: inline，图片/PDF 等可直接预览）
     */
    @Operation(summary = "附件预览")
    @GetMapping("/preview")
    public void preview(@NotNull(message = "附件ID不能为空") Long id, HttpServletResponse response) {
        doServe(id, response, false);
    }

    /**
     * 统一输出附件流：设置响应头后，由存储后端把文件内容写入响应输出流
     */
    private void doServe(Long id, HttpServletResponse response, boolean forceDownload) {
        SysAttachment att = attachmentService.findById(id);
        SysStorageConfig config = storageConfigService.findById(att.getConfigId());
        FileStorage storage = fileStorageManager.getStorage(config);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(StrUtil.emptyToDefault(att.getContentType(), "application/octet-stream"));
        String ext = StrUtil.emptyToDefault(att.getFileExt(), "");
        // 下载文件名用无横线的随机 UUID（simpleUUID），保持与存储 key 解耦
        String fileName = IdUtil.simpleUUID() + (StrUtil.isBlank(ext) ? "" : "." + ext);
        String encoded = encodeFileName(fileName);
        String disposition = (forceDownload ? "attachment" : "inline")
                + "; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded;
        response.setHeader("Content-Disposition", disposition);
        response.setHeader("Filename", fileName);
        if (att.getFileSize() != null && att.getFileSize() > 0) {
            response.setContentLengthLong(att.getFileSize());
        }
        try (OutputStream out = response.getOutputStream()) {
            storage.download(att.getFileKey(), out);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "文件输出失败：" + e.getMessage());
        }
    }

    /**
     * 文件名按 RFC 5987 编码（兼容中文/空格，空格转 %20）
     */
    private String encodeFileName(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return "download";
        }
    }

    /**
     * 填充访问地址与预览链接（均不依赖已废弃的 file_url 字段，由存储配置实时拼出）：
     * fileUrl 对所有附件返回（通用访问/下载地址），previewUrl 仅图片返回（可内联渲染）。
     */
    private void fillUrls(SysAttachment att, AttachmentVO vo) {
        vo.setFileUrl(attachmentUrlComponent.getAccessUrl(att));
        if (attachmentUrlComponent.isImage(att)) {
            vo.setPreviewUrl(attachmentUrlComponent.getAccessUrl(att));
        }
    }

    /**
     * 删除附件（同步删除物理文件）
     */
    @Operation(summary = "删除附件")
    @SaCheckPermission("system:file:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "附件ID不能为空") Long id) {
        SysAttachment att = attachmentService.findById(id);
        Assert.isTrue(att.getBizId() == 0, "附件已关联，无法删除");
        SysStorageConfig config = storageConfigService.findById(att.getConfigId());
        FileStorage storage = fileStorageManager.getStorage(config);
        try {
            storage.delete(att.getFileKey());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "删除物理文件失败：" + e.getMessage());
        }
        attachmentService.removeAttachment(id);
        return Result.success();
    }

    /**
     * 获取文件扩展名（不含点，转小写）
     */
    private String parseExt(String filename) {
        if (StrUtil.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
