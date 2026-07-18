package com.platform.component.file;

import cn.hutool.core.util.StrUtil;
import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import com.platform.starter.file.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 分片上传组件
 *
 * <p>负责分片在服务器临时目录的落盘、查询、合并与清理，不涉及具体存储后端。
 * 分片以 {@code <chunkTempDir>/<identifier>/<chunkNumber>.part} 形式存放，
 * 合并时将各分片按顺序拼接为单个临时文件交回调用方，由调用方决定最终存储后端。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkUploadComponent {

    private final FileUploadProperties fileUploadProperties;

    /**
     * 保存单个分片（追加写入到对应 .part 文件）
     *
     * @param identifier   文件唯一标识（通常为文件 MD5 或前端生成的随机串）
     * @param chunkNumber  分片序号（从 1 开始）
     * @param chunkStream  分片输入流
     */
    public void saveChunk(String identifier, int chunkNumber, InputStream chunkStream) {
        if (StrUtil.isBlank(identifier)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "文件标识不能为空");
        }
        if (chunkNumber < 1) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "分片序号非法");
        }
        File dir = chunkDir(identifier);
        try {
            // 幂等创建，目录已存在不会报错；并发上传同一 identifier 的分片时也不会因 mkdirs() 返回 false 而失败
            Files.createDirectories(dir.toPath());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "创建分片目录失败：" + e.getMessage());
        }
        File part = new File(dir, chunkNumber + ".part");
        try (OutputStream os = Files.newOutputStream(part.toPath())) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = chunkStream.read(buf)) > 0) {
                os.write(buf, 0, n);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "分片写入失败：" + e.getMessage());
        }
    }

    /**
     * 查询已上传的分片序号列表（升序）
     *
     * @param identifier 文件唯一标识
     * @return 已上传分片序号
     */
    public List<Integer> listChunks(String identifier) {
        File dir = chunkDir(identifier);
        List<Integer> list = new ArrayList<>();
        if (!dir.exists()) {
            return list;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".part"));
        if (files == null) {
            return list;
        }
        for (File f : files) {
            String name = f.getName();
            list.add(Integer.parseInt(name.substring(0, name.indexOf('.'))));
        }
        list.sort(Integer::compareTo);
        return list;
    }

    /**
     * 判断分片是否已全部上传
     */
    public boolean isAllUploaded(String identifier, int totalChunks) {
        List<Integer> uploaded = listChunks(identifier);
        if (uploaded.size() != totalChunks) {
            return false;
        }
        for (int i = 1; i <= totalChunks; i++) {
            if (!uploaded.contains(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 合并分片为单个临时文件（调用方负责删除返回的临时文件）
     *
     * @param identifier   文件唯一标识
     * @param totalChunks  总分片数
     * @param fileName     原始文件名（用于生成合并文件名，已做路径穿越防护）
     * @return 合并后的临时文件
     */
    public File merge(String identifier, int totalChunks, String fileName) {
        if (!isAllUploaded(identifier, totalChunks)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "分片未全部上传，无法合并");
        }
        File dir = chunkDir(identifier);
        String safeName = StrUtil.isBlank(fileName) ? "merge.tmp" : sanitize(fileName);
        File merged = new File(dir, "merged_" + UUID.randomUUID().toString().replace("-", "") + "_" + safeName);
        try (OutputStream os = Files.newOutputStream(merged.toPath())) {
            for (int i = 1; i <= totalChunks; i++) {
                File part = new File(dir, i + ".part");
                try (InputStream is = Files.newInputStream(part.toPath())) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) > 0) {
                        os.write(buf, 0, n);
                    }
                }
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "分片合并失败：" + e.getMessage());
        }
        return merged;
    }

    /**
     * 清理分片临时目录（合并完成或上传失败回滚时调用）
     *
     * @param identifier 文件唯一标识
     */
    public void cleanup(String identifier) {
        File dir = chunkDir(identifier);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            dir.delete();
        }
    }

    private File chunkDir(String identifier) {
        String base = fileUploadProperties.getChunkTempDir();
        if (!base.endsWith(File.separator)) {
            base = base + File.separator;
        }
        return new File(base + sanitize(identifier));
    }

    /**
     * 去除路径分隔符，防止目录穿越
     */
    private String sanitize(String name) {
        return name.replaceAll("[\\\\/]", "_");
    }
}
