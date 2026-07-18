package com.platform.starter.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传配置属性类
 *
 * <p>对应 application.yml 中 {@code file.upload} 前缀的配置项，
 * 统一管理文件上传大小限制、存储根路径、允许的文件扩展名、URL访问前缀等。</p>
 *
 * @author platform
 */
@Data
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 单文件最大大小（单位：MB，默认 10MB） */
    private long maxSize = 10L;

    /** 存储根路径（默认 ./uploads） */
    private String path = "./uploads";

    /** 允许的文件扩展名（小写，默认图片+文档） */
    private List<String> allowedTypes = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "zip", "rar"
    );

    /** URL 访问前缀（默认 /file/） */
    private String urlPrefix = "/file/";

    /** 是否按日期分目录存储（默认开启） */
    private boolean datePath = true;

    /** 分片上传临时目录（默认 ./chunks，绝对路径或相对路径均可） */
    private String chunkTempDir = "./chunks";
}
