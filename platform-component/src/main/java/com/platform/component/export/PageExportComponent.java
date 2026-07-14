package com.platform.component.export;

import com.alibaba.excel.EasyExcel;
import com.platform.common.exception.BusinessException;
import com.platform.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 分页导出组件
 *
 * <p>基于 EasyExcel 封装通用的 Excel 导出逻辑，支持任意实体/VO 列表导出。
 * 业务层注入后直接调用即可，避免重复编写导出代码。</p>
 *
 * <p><b>调用规范</b>：</p>
 * <pre>{@code
 * @Autowired
 * private PageExportComponent exportComponent;
 *
 * exportComponent.export(response, "用户列表", UserExportVO.class, userList);
 * }</pre>
 *
 * @author platform
 */
@Slf4j
@Component
public class PageExportComponent {

    /**
     * 导出 Excel
     *
     * @param response  HTTP 响应对象
     * @param fileName  文件名（不含扩展名）
     * @param headClass 表头类（标注了 @ExcelProperty 的类）
     * @param data      数据列表
     * @param <T>       数据类型
     */
    public <T> void export(HttpServletResponse response, String fileName,
                           Class<T> headClass, List<T> data) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + encodedFileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), headClass)
                    .sheet(fileName)
                    .doWrite(data);

            log.info("[Excel导出] 成功, fileName={}, dataSize={}", fileName, Objects.isNull(data) ? 0 : data.size());
        } catch (IOException e) {
            log.error("[Excel导出] 失败, fileName={}", fileName, e);
            throw new BusinessException(ErrorCode.FAIL, "Excel导出失败：" + e.getMessage());
        }
    }
}
