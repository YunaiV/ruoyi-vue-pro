package cn.iocoder.yudao.framework.excel.core.handler;

import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ColumnWidthMatchStyleStrategy} 的单元测试
 *
 * @author 芋道源码
 */
public class ColumnWidthMatchStyleStrategyTest {

    @Test
    public void testWrite_emptyChineseHeader() throws Exception {
        // 调用
        try (XSSFWorkbook workbook = write(Collections.emptyList())) {
            // 断言：空列表导出的中文短表头也保留足够宽度
            assertEquals("标题", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            assertTrue(workbook.getSheetAt(0).getColumnWidth(0) >= 8 * 256);
        }
    }

    @Test
    public void testWrite_longestValue() throws Exception {
        // mock 数据
        TestRow row = new TestRow();
        row.setName("自动化测试工作项标题");

        // 调用
        try (XSSFWorkbook workbook = write(Collections.singletonList(row))) {
            // 断言：数据内容比表头长时继续扩展列宽
            assertTrue(workbook.getSheetAt(0).getColumnWidth(0) > 8 * 256);
            assertEquals(row.getName(), workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        }
    }

    @Test
    public void testWrite_maxColumnWidth() throws Exception {
        // mock 数据
        TestRow row = new TestRow();
        row.setName("测试".repeat(100));

        // 调用
        try (XSSFWorkbook workbook = write(Collections.singletonList(row))) {
            // 断言：留白后仍不超过 Excel 的列宽上限
            assertEquals(255 * 256, workbook.getSheetAt(0).getColumnWidth(0));
        }
    }

    private XSSFWorkbook write(List<TestRow> rows) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FesodSheet.write(outputStream, TestRow.class)
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy()).sheet().doWrite(rows);
        return new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    public static class TestRow {

        @ExcelProperty("标题")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
