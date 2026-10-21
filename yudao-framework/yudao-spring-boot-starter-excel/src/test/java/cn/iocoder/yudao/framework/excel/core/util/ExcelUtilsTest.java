package cn.iocoder.yudao.framework.excel.core.util;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.biz.system.dict.DictDataCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link ExcelUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class ExcelUtilsTest extends BaseMockitoUnitTest {

    private static final String DICT_TYPE = "test_status";

    @Mock
    private DictDataCommonApi dictDataApi;

    @BeforeEach
    public void setUp() {
        DictFrameworkUtils.init(dictDataApi);
        DictFrameworkUtils.clearCache();
    }

    /**
     * 字段未配置 converter 的 Long 编号，需要导出为文本，避免丢失精度
     */
    @Test
    public void testWrite_class() throws Exception {
        // mock 数据
        mockDictDatas();
        // 准备参数
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<TestUser> list = Arrays.asList(
                new TestUser().setId(1981234567890123456L).setAge(18).setStatus(0),
                new TestUser().setId(Long.MAX_VALUE).setAge(20).setStatus(1));

        // 调用
        ExcelUtils.write(response, "用户.xls", "数据", TestUser.class, list);
        // 断言
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            Sheet sheet = workbook.getSheet("数据");
            // 断言 Long 编号
            assertStringCell("1981234567890123456", sheet.getRow(1).getCell(0));
            assertStringCell("9223372036854775807", sheet.getRow(2).getCell(0));
            // 断言普通数值
            assertNumericCell(18, sheet.getRow(1).getCell(1));
            assertNumericCell(20, sheet.getRow(2).getCell(1));
            // 断言字典转换
            assertStringCell("开启", sheet.getRow(1).getCell(2));
            assertStringCell("关闭", sheet.getRow(2).getCell(2));
            // 断言下拉框
            assertNotNull(workbook.getSheet("字典sheet"));
            assertEquals(1, sheet.getDataValidations().size());
            // 断言列宽：19 位 Long 字符串，加上 2 个字符的留白
            assertEquals((19 + 2) * 256, sheet.getColumnWidth(0));
        }
    }

    @Test
    public void testWrite_dynamicHead() throws Exception {
        // 准备参数
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<List<String>> head = Arrays.asList(Collections.singletonList("编号"), Collections.singletonList("年龄"));
        List<List<Object>> list = Arrays.asList(
                Arrays.asList(1981234567890123456L, 18),
                Arrays.asList(Long.MAX_VALUE, 20));

        // 调用
        ExcelUtils.write(response, "用户.xls", "数据", head, list);
        // 断言
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            Sheet sheet = workbook.getSheet("数据");
            assertStringCell("编号", sheet.getRow(0).getCell(0));
            // 断言 Long 编号
            assertStringCell("1981234567890123456", sheet.getRow(1).getCell(0));
            assertStringCell("9223372036854775807", sheet.getRow(2).getCell(0));
            // 断言普通数值
            assertNumericCell(18, sheet.getRow(1).getCell(1));
            assertNumericCell(20, sheet.getRow(2).getCell(1));
        }
    }

    @Test
    public void testRead_maxRowCount() throws Exception {
        // mock 数据
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FesodSheet.write(outputStream, TestRow.class).sheet().doWrite(Arrays.asList(
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

    private void mockDictDatas() {
        List<DictDataRespDTO> dictDatas = ListUtil.of(
                randomPojo(DictDataRespDTO.class, o -> o.setDictType(DICT_TYPE).setValue("0").setLabel("开启")),
                randomPojo(DictDataRespDTO.class, o -> o.setDictType(DICT_TYPE).setValue("1").setLabel("关闭"))
        );
        when(dictDataApi.getDictDataList(eq(DICT_TYPE))).thenReturn(dictDatas);
    }

    private static void assertStringCell(String expected, Cell cell) {
        assertEquals(CellType.STRING, cell.getCellType());
        assertEquals(expected, cell.getStringCellValue());
    }

    private static void assertNumericCell(double expected, Cell cell) {
        assertEquals(CellType.NUMERIC, cell.getCellType());
        assertEquals(expected, cell.getNumericCellValue());
    }

    @Data
    @Accessors(chain = true)
    public static class TestUser {

        @ExcelProperty("编号")
        private Long id;

        @ExcelProperty("年龄")
        private Integer age;

        @ExcelProperty(value = "状态", converter = DictConvert.class)
        @DictFormat(DICT_TYPE)
        @ExcelColumnSelect(dictType = DICT_TYPE)
        private Integer status;

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
