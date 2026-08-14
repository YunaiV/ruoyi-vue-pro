package cn.iocoder.yudao.framework.excel.core.util;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.annotation.ExcelProperty;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ExcelUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class ExcelUtilsTest {

    @Test
    public void testRead_maxRowCount() throws Exception {
        // mock 数据
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FastExcelFactory.write(outputStream, TestRow.class).sheet().doWrite(Arrays.asList(
                new TestRow("员工一"), new TestRow("员工二"), new TestRow("员工三")));
        MockMultipartFile file = new MockMultipartFile(
                "file", "employees.xlsx", null, outputStream.toByteArray());

        // 调用
        List<TestRow> rows = ExcelUtils.read(file, TestRow.class, 2);

        // 断言
        assertEquals(2, rows.size());
        assertEquals("员工一", rows.get(0).getName());
        assertEquals("员工二", rows.get(1).getName());
    }

    public static class TestRow {

        @ExcelProperty("姓名")
        private String name;

        public TestRow() {
        }

        public TestRow(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
