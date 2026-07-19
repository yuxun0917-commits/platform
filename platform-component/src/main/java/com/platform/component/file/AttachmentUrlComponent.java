package com.platform.component.file;

import cn.hutool.core.util.StrUtil;
import com.platform.common.entity.admin.SysAttachment;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.service.service.SysAttachmentService;
import com.platform.service.service.SysStorageConfigService;
import com.platform.starter.file.FileStorageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 附件访问/预览链接组件
 *
 * <p>统一封装“根据附件返回可直接访问的预览链接”这一公共能力，供 Controller 在
 * 上传成功返回、列表页、详情页等场景复用，也供后续用户头像等其它业务模块调用。</p>
 *
 * <p>链接优先使用已落库的 {@code SysAttachment.fileUrl}（上传时已按存储后端拼好，
 * 如本地 {@code http://localhost:8080/file/yyyy/MM/dd/uuid.png}），为空时再按
 * 存储配置实时拼接，兼容历史数据或链接失效的场景。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentUrlComponent {

    private final FileStorageManager fileStorageManager;
    private final SysStorageConfigService storageConfigService;
    private final SysAttachmentService attachmentService;

    /** 图片扩展名集合（小写，不含点） */
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");

    /**
     * 根据附件ID返回访问/预览地址（公共方法）
     *
     * <p>由存储配置 + 存储键实时拼出，不依赖已废弃的 file_url 字段（URL 属可派生数据）。</p>
     *
     * @param attachmentId 附件ID
     * @return 可直接在浏览器/&lt;img&gt; 中访问的链接
     */
    public String getAccessUrl(Long attachmentId) {
        return getAccessUrl(attachmentService.findById(attachmentId));
    }

    /**
     * 根据附件实体返回访问/预览地址（公共方法）
     *
     * <p>按 {@code sys_storage_config.domain + file_key} 实时拼出，
     * 本地为 {@code http://localhost:8080/file/...} 直链，OSS/COS/MinIO 为各自域名/桶地址。</p>
     *
     * @param attachment 附件实体
     * @return 可直接在浏览器/&lt;img&gt; 中访问的链接
     */
    public String getAccessUrl(SysAttachment attachment) {
        SysStorageConfig config = storageConfigService.findById(attachment.getConfigId());
        return fileStorageManager.getAccessUrl(config, attachment.getFileKey());
    }

    /**
     * 根据附件ID返回预览链接（公共方法）
     *
     * @param attachmentId 附件ID
     * @return 图片返回预览链接，否则 null
     */
    public String getPreviewUrl(Long attachmentId) {
        return getPreviewUrl(attachmentService.findById(attachmentId));
    }

    /**
     * 根据附件实体返回预览链接（公共方法）
     *
     * <p>仅图片返回可直链的预览地址；非图片返回 null（前端改用 {@link #getAccessUrl} / 下载接口）。</p>
     *
     * @param attachment 附件实体
     * @return 图片返回预览链接，否则 null
     */
    public String getPreviewUrl(SysAttachment attachment) {
        return isImage(attachment) ? getAccessUrl(attachment) : null;
    }

    /**
     * 判断附件是否为图片（决定是否在 VO 中填充 previewUrl）
     *
     * @param attachment 附件实体
     * @return true 表示图片
     */
    public boolean isImage(SysAttachment attachment) {
        return isImage(attachment.getContentType(), attachment.getFileExt());
    }

    /**
     * 根据 MIME 类型与扩展名判断是否为图片
     *
     * <p>优先按 content_type 前缀 {@code image/} 判断；content_type 为空时退回扩展名判断。</p>
     *
     * @param contentType MIME 类型（可为空）
     * @param ext         扩展名（不含点，可为空）
     * @return true 表示图片
     */
    public boolean isImage(String contentType, String ext) {
        if (StrUtil.isNotBlank(contentType)) {
            return contentType.startsWith("image/");
        }
        return isImageExt(ext);
    }

    private boolean isImageExt(String ext) {
        return StrUtil.isNotBlank(ext) && IMAGE_EXT.contains(ext.toLowerCase());
    }
}
