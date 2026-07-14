package com.platform.admin.controller.attachment;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.attachment.AttachmentVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.entity.admin.SysAttachment;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.service.service.SysAttachmentService;
import com.platform.service.service.SysStorageConfigService;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileStorageManager;
import com.platform.starter.file.FileUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    /**
     * 文件上传
     *
     * @param file     上传文件
     * @param configId 指定存储配置ID（为空则走默认存储）
     * @param bizType  业务类型（如 avatar/article）
     * @param bizId    业务ID
     * @return 附件信息
     */
    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public Result upload(@NotNull(message = "上传文件不能为空") @RequestParam("file") MultipartFile file,
                         Long configId,
                         String bizType,
                         String bizId) {
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
        attach.setFileUrl(result.getFileUrl());
        attach.setFileExt(parseExt(file.getOriginalFilename()));
        attach.setContentType(file.getContentType());
        attach.setFileSize(file.getSize());
        attach.setBizType(StrUtil.trimToEmpty(bizType));
        attach.setBizId(StrUtil.trimToEmpty(bizId));
        attachmentService.save(attach);

        AttachmentVO vo = mapperFacade.map(attach, AttachmentVO.class);
        return Result.success(vo);
    }

    /**
     * 附件分页列表
     */
    @Operation(summary = "附件列表")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Long configId, String bizType, String keyword) {
        Paging<SysAttachment> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>(8);
        paramsMap.put("configId", configId);
        paramsMap.put("bizType", StrUtil.trim(bizType));
        paramsMap.put("keyword", StrUtil.trim(keyword));
        attachmentService.paging(paging, paramsMap);
        paging.convert(att -> mapperFacade.map(att, AttachmentVO.class));
        return Result.success(paging);
    }

    /**
     * 附件详情
     */
    @Operation(summary = "附件详情")
    @GetMapping("/view")
    public Result view(@NotNull(message = "附件ID不能为空") Long id) {
        SysAttachment att = attachmentService.findById(id);
        AttachmentVO vo = mapperFacade.map(att, AttachmentVO.class);
        return Result.success(vo);
    }

    /**
     * 删除附件（同步删除物理文件）
     */
    @Operation(summary = "删除附件")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "附件ID不能为空") Long id) {
        SysAttachment att = attachmentService.findById(id);
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
