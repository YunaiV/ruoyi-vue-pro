package cn.iocoder.yudao.framework.excel.core.util;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import cn.idev.excel.read.listener.ReadListener;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.excel.core.handler.ColumnWidthMatchStyleStrategy;
import cn.iocoder.yudao.framework.excel.core.handler.SelectSheetWriteHandler;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Excel 工具类
 *
 * @author 芋道源码
 */
public class ExcelUtils {

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response  响应
     * @param filename  文件名
     * @param sheetName Excel sheet 名
     * @param head      Excel head 头
     * @param data      数据列表哦
     * @param <T>       泛型，保证 head 和 data 类型的一致性
     * @throws IOException 写入失败的情况
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        // 输出 Excel
        FastExcelFactory.write(response.getOutputStream(), head)
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                .registerWriteHandler(new SelectSheetWriteHandler(head)) // 基于固定 sheet 实现下拉框
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .sheet(sheetName).doWrite(data);
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(filename));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    /**
     * 将动态表头和数据以 Excel 响应给前端
     *
     * @param response 响应
     * @param filename 文件名
     * @param sheetName Excel sheet 名
     * @param head Excel head 头
     * @param data 数据列表
     * @throws IOException 写入失败的情况
     */
    public static void write(HttpServletResponse response, String filename, String sheetName,
                             List<List<String>> head, List<List<Object>> data) throws IOException {
        // 输出 Excel
        FastExcelFactory.write(response.getOutputStream())
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .head(head).sheet(sheetName).doWrite(data);
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(filename));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }
        // 参考 https://t.zsxq.com/zM77F 帖子，增加 try 处理，兼容 windows 场景
        try (InputStream inputStream = file.getInputStream()) {
            return FastExcelFactory.read(inputStream, head, null)
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .doReadAllSync();
        }
    }

    /**
     * 读取指定数量以内的 Excel 数据，达到上限后停止继续解析。
     *
     * @param file Excel 文件，可为空
     * @param head Excel 表头类型
     * @param maxRowCount 最大读取行数
     * @return Excel 数据
     */
    public static <T> List<T> read(MultipartFile file, Class<T> head, int maxRowCount) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }
        if (maxRowCount <= 0) {
            throw new IllegalArgumentException("maxRowCount must be greater than 0");
        }
        List<T> rows = new ArrayList<>(Math.min(maxRowCount, 1024));
        try (InputStream inputStream = file.getInputStream()) {
            FastExcelFactory.read(inputStream, head, new ReadListener<T>() {

                @Override
                public void invoke(T data, AnalysisContext context) {
                    rows.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 无需处理
                }

                @Override
                public boolean hasNext(AnalysisContext context) {
                    return rows.size() < maxRowCount;
                }

            }).autoCloseStream(false).doReadAll();
        }
        return rows;
    }

    /**
     * 读取动态表头 Excel 数据
     *
     * @param file Excel 文件，可为空
     * @return 按列下标存储的行数据
     * @throws IOException 读取失败
     */
    public static List<Map<Integer, String>> read(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }
        // 参考 https://t.zsxq.com/zM77F 帖子，增加 try 处理，兼容 windows 场景
        try (InputStream inputStream = file.getInputStream()) {
            return FastExcelFactory.read(inputStream)
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .headRowNumber(1)
                    .sheet()
                    .doReadSync();
        }
    }

}
