package com.platform.starter.file;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件上传结果
 *
 * <p>封装一次上传后需要落库的核心元数据，供调用方写入 {@code sys_attachment} 表使用。</p>
 *
 * @author platform
 */
@Data
public class FileUploadResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 存储键/相对路径（本地=日期目录+UUID；对象存储=object key） */
    private String fileKey;

    /** 文件访问地址 */
    private String fileUrl;

    /** 存储桶/容器名（对象存储用，本地留空） */
    private String bucket;

    public FileUploadResult() {
    }

    public FileUploadResult(String fileKey, String fileUrl, String bucket) {
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
        this.bucket = bucket;
    }
}
