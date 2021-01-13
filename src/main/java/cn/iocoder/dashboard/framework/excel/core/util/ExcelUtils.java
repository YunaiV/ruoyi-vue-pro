package cn.iocoder.dashboard.framework.excel.core.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Excel 工具类
 */
public class ExcelUtils {

    public static void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<?> head, List<?> data) throws IOException {
        // 设置 header 和 contentType
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        // 输出 Excel
        EasyExcel.write(response.getOutputStream(), head)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                .sheet(sheetName).doWrite(data);
    }

}
