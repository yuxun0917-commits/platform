package com.platform.admin.controller.attachment;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.attachment.AttachmentVO;
import com.platform.common.entity.admin.SysAttachment;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.AttachmentBizTypeEnum;
import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.component.file.AttachmentUrlComponent;
import com.platform.component.file.ChunkUploadComponent;
import com.platform.service.service.SysAttachmentService;
import com.platform.service.service.SysStorageConfigService;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileStorageManager;
import com.platform.starter.file.FileUploadProperties;
import com.platform.starter.file.FileUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 附件分片上传控制器
 *
 * <p>提供大文件分片上传能力，支持断点续传：</p>
 * <ul>
 *   <li>POST /attachment/chunk/upload - 上传单个分片（按 identifier + chunkNumber 落盘）</li>
 *   <li>GET  /attachment/chunk/check  - 查询已上传分片（断点续传，返回已上传序号与是否完成）</li>
 *   <li>POST /attachment/chunk/merge  - 合并分片 → 走指定存储后端 → 落库 sys_attachment</li>
 * </ul>
 *
 * <p>分片在服务器临时目录存放，合并后交由 {@link FileStorage} 上传至最终存储后端，
 * 结束后清理临时文件。客户端需自行计算 identifier（如文件 MD5）并顺序上传分片。</p>
 *
 * @author platform
 */
@Tag(name = "附件分片上传")
@Validated
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/attachment/chunk")
public class FileChunkController {

    private final FileStorageManager fileStorageManager;
    private final SysStorageConfigService storageConfigService;
    private final SysAttachmentService attachmentService;
    private final ChunkUploadComponent chunkUploadComponent;
    private final FileUploadProperties fileUploadProperties;
    private final MapperFacade mapperFacade;
    private final AttachmentUrlComponent attachmentUrlComponent;

    /**
     * 上传单个分片
     */
    @Operation(summary = "上传分片")
    @SaCheckPermission("system:file:upload")
    @PostMapping("/upload")
    public Result uploadChunk(@NotNull(message = "分片文件不能为空") @RequestParam("file") MultipartFile chunk,
                              @NotBlank(message = "文件标识不能为空") @RequestParam("identifier") String identifier,
                              @Min(value = 1, message = "分片序号从1开始") @RequestParam("chunkNumber") int chunkNumber,
                              @Min(value = 1, message = "总分片数必须≥1") @RequestParam("totalChunks") int totalChunks) {
        Assert.isTrue(!chunk.isEmpty(), "分片文件不能为空");
        try {
            chunkUploadComponent.saveChunk(identifier, chunkNumber, chunk.getInputStream());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "读取分片失败：" + e.getMessage());
        }
        List<Integer> uploaded = chunkUploadComponent.listChunks(identifier);
        Map<String, Object> data = new HashMap<>(4);
        data.put("uploaded", uploaded);
        data.put("uploadedCount", uploaded.size());
        data.put("totalChunks", totalChunks);
        return Result.success(data);
    }

    /**
     * 查询已上传分片（断点续传）
     */
    @Operation(summary = "检查已上传分片")
    @SaCheckPermission("system:file:upload")
    @GetMapping("/check")
    public Result check(@NotBlank(message = "文件标识不能为空") @RequestParam("identifier") String identifier,
                        @Min(value = 1, message = "总分片数必须≥1") @RequestParam("totalChunks") int totalChunks) {
        List<Integer> uploaded = chunkUploadComponent.listChunks(identifier);
        Map<String, Object> data = new HashMap<>(4);
        data.put("uploaded", uploaded);
        data.put("uploadedCount", uploaded.size());
        data.put("totalChunks", totalChunks);
        data.put("finished", chunkUploadComponent.isAllUploaded(identifier, totalChunks));
        return Result.success(data);
    }

    /**
     * 合并分片并完成上传（落库 sys_attachment）
     */
    @Operation(summary = "合并分片并上传")
    @SaCheckPermission("system:file:upload")
    @PostMapping("/merge")
    public Result merge(@NotBlank(message = "文件标识不能为空") @RequestParam("identifier") String identifier,
                        @NotBlank(message = "文件名不能为空") @RequestParam("fileName") String fileName,
                        @Min(value = 1, message = "总分片数必须≥1") @RequestParam("totalChunks") int totalChunks,
                        Long configId,
                        Integer bizType,
                        Long bizId,
                        String contentType) {
        // 类型校验（分片上传绕过单文件大小限制，但需校验类型）
        String ext = parseExt(fileName);
        if (!fileUploadProperties.getAllowedTypes().contains(ext.toLowerCase())) {
            chunkUploadComponent.cleanup(identifier);
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                    "文件类型不允许，支持的类型：" + fileUploadProperties.getAllowedTypes());
        }
        if (!chunkUploadComponent.isAllUploaded(identifier, totalChunks)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "分片未全部上传，无法合并");
        }

        SysStorageConfig config = (configId != null)
                ? storageConfigService.findById(configId)
                : storageConfigService.findDefault();
        File merged = chunkUploadComponent.merge(identifier, totalChunks, fileName);
        try {
            FileStorage storage = fileStorageManager.getStorage(config);
            FileUploadResult result = storage.upload(merged, fileName);

            SysAttachment attach = new SysAttachment();
            attach.setConfigId(config.getId());
            attach.setFileName(fileName);
            attach.setFileKey(result.getFileKey());
            attach.setFileExt(ext);
            attach.setContentType(StrUtil.blankToDefault(contentType, "application/octet-stream"));
            attach.setFileSize(merged.length());
            attach.setBizType(bizType);
            attach.setBizId(bizId);
            attachmentService.save(attach);

            AttachmentVO vo = mapperFacade.map(attach, AttachmentVO.class);
            vo.setBizTypeDesc(AttachmentBizTypeEnum.getDescByCode(attach.getBizType()));
            fillUrls(attach, vo);
            return Result.success(vo);
        } finally {
            if (merged.exists() && !merged.delete()) {
                log.warn("[分片上传] 清理合并临时文件失败: {}", merged.getAbsolutePath());
            }
            chunkUploadComponent.cleanup(identifier);
        }
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

    /**
     * 填充访问地址与预览链接（fileUrl 对所有附件返回；previewUrl 仅图片返回，可内联渲染）。
     * 与 SysAttachmentController 一致：URL 不落库，由存储配置实时拼出。
     */
    private void fillUrls(SysAttachment att, AttachmentVO vo) {
        vo.setFileUrl(attachmentUrlComponent.getAccessUrl(att));
        if (attachmentUrlComponent.isImage(att)) {
            vo.setPreviewUrl(attachmentUrlComponent.getAccessUrl(att));
        }
    }
}
