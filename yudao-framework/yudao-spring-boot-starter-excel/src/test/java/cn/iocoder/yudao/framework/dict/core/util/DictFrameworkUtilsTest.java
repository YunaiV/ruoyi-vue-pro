package cn.iocoder.yudao.framework.dict.core.util;

import cn.hutool.core.collection.ListUtil;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.iocoder.yudao.framework.common.biz.system.dict.DictDataCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.convert.MultiDictConvert;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * {@link DictFrameworkUtils} 的单元测试
 */
public class DictFrameworkUtilsTest extends BaseMockitoUnitTest {

    @Mock
    private DictDataCommonApi dictDataApi;

    @BeforeEach
    public void setUp() {
        DictFrameworkUtils.init(dictDataApi);
        DictFrameworkUtils.clearCache();
    }

    @Test
    public void testParseDictDataLabel() {
        // mock 数据
        List<DictDataRespDTO> dictDatas = ListUtil.of(
                randomPojo(DictDataRespDTO.class, o -> o.setDictType("animal").setValue("cat").setLabel("猫")),
                randomPojo(DictDataRespDTO.class, o -> o.setDictType("animal").setValue("dog").setLabel("狗"))
        );
        // mock 方法
        when(dictDataApi.getDictDataList(eq("animal"))).thenReturn(dictDatas);

        // 断言返回值
        assertEquals("狗", DictFrameworkUtils.parseDictDataLabel("animal", "dog"));
    }

    @Test
    public void testParseDictDataValue() {
        // mock 数据
        List<DictDataRespDTO> dictDatas = ListUtil.of(
                randomPojo(DictDataRespDTO.class, o -> o.setDictType("animal").setValue("cat").setLabel("猫")),
                randomPojo(DictDataRespDTO.class, o -> o.setDictType("animal").setValue("dog").setLabel("狗"))
        );
        // mock 方法
        when(dictDataApi.getDictDataList(eq("animal"))).thenReturn(dictDatas);

        // 断言返回值
        assertEquals("dog", DictFrameworkUtils.parseDictDataValue("animal", "狗"));
    }

}

/**
 * {@link MultiDictConvertTest} 的单元测试
 */
@ExtendWith(MockitoExtension.class)
class MultiDictConvertTest {

    private MultiDictConvert converter;
    private ExcelContentProperty contentProperty;
    private Field field;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        converter = new MultiDictConvert();
        // 模拟字段上的 @DictFormat 注解
        field = TestClass.class.getDeclaredField("dictField");
        contentProperty = mock(ExcelContentProperty.class);
        when(contentProperty.getField()).thenReturn(field);
    }

    // ---------- 导出测试 ----------
    @Test
    void testConvertToExcelData_StringWithComma() {
        try (MockedStatic<DictFrameworkUtils> utils = mockStatic(DictFrameworkUtils.class)) {
            utils.when(() -> DictFrameworkUtils.parseDictDataLabel("testDict", "1")).thenReturn("男");
            utils.when(() -> DictFrameworkUtils.parseDictDataLabel("testDict", "2")).thenReturn("女");

            WriteCellData<String> result = converter.convertToExcelData("1,2", contentProperty, null);
            assertEquals("男、女", result.getStringValue());
        }
    }

    @Test
    void testConvertToExcelData_ListInput() {
        try (MockedStatic<DictFrameworkUtils> utils = mockStatic(DictFrameworkUtils.class)) {
            utils.when(() -> DictFrameworkUtils.parseDictDataLabel("testDict", "1")).thenReturn("男");
            utils.when(() -> DictFrameworkUtils.parseDictDataLabel("testDict", "2")).thenReturn("女");

            List<String> list = Arrays.asList("1", "2");
            WriteCellData<String> result = converter.convertToExcelData(list, contentProperty, null);
            assertEquals("男、女", result.getStringValue());
        }
    }

    @Test
    void testConvertToExcelData_EmptyValue() {
        WriteCellData<String> result = converter.convertToExcelData(null, contentProperty, null);
        assertEquals("", result.getStringValue());

        result = converter.convertToExcelData("", contentProperty, null);
        assertEquals("", result.getStringValue());
    }

    @Test
    void testConvertToExcelData_UnmatchedLabel() {
        try (MockedStatic<DictFrameworkUtils> utils = mockStatic(DictFrameworkUtils.class)) {
            utils.when(() -> DictFrameworkUtils.parseDictDataLabel("testDict", "999")).thenReturn(null);

            WriteCellData<String> result = converter.convertToExcelData("999", contentProperty, null);
            assertEquals("", result.getStringValue()); // 未匹配返回空字符串
            // 同时应打印 error 日志（可通过 logback 测试捕获）
        }
    }

    // ---------- 导入测试 ----------
    @Test
    void testConvertToJavaData_ChineseDunhao() {
        try (MockedStatic<DictFrameworkUtils> utils = mockStatic(DictFrameworkUtils.class)) {
            utils.when(() -> DictFrameworkUtils.parseDictDataValue("testDict", "男")).thenReturn("1");
            utils.when(() -> DictFrameworkUtils.parseDictDataValue("testDict", "女")).thenReturn("2");

            ReadCellData readCellData = mock(ReadCellData.class);
            when(readCellData.getStringValue()).thenReturn("男、女");

            Object result = converter.convertToJavaData(readCellData, contentProperty, null);
            assertEquals("1,2", result); // 字段类型是 String，返回拼接字符串
        }
    }

    @Test
    void testConvertToJavaData_EnglishComma() {
        try (MockedStatic<DictFrameworkUtils> utils = mockStatic(DictFrameworkUtils.class)) {
            utils.when(() -> DictFrameworkUtils.parseDictDataValue("testDict", "男")).thenReturn("1");
            utils.when(() -> DictFrameworkUtils.parseDictDataValue("testDict", "女")).thenReturn("2");

            ReadCellData readCellData = mock(ReadCellData.class);
            when(readCellData.getStringValue()).thenReturn("男,女");

            Object result = converter.convertToJavaData(readCellData, contentProperty, null);
            assertEquals("1,2", result);
        }
    }

    @Test
    void testConvertToJavaData_ChineseComma() {
        try (MockedStatic<DictFrameworkUtils> utils = mockStatic(DictFrameworkUtils.class)) {
            utils.when(() -> DictFrameworkUtils.parseDictDataValue("testDict", "男")).thenReturn("1");
            utils.when(() -> DictFrameworkUtils.parseDictDataValue("testDict", "女")).thenReturn("2");

            ReadCellData readCellData = mock(ReadCellData.class);
            when(readCellData.getStringValue()).thenReturn("男，女");

            Object result = converter.convertToJavaData(readCellData, contentProperty, null);
            assertEquals("1,2", result);
        }
    }

    @Test
    void testConvertToJavaData_Empty() {
        ReadCellData readCellData = mock(ReadCellData.class);
        when(readCellData.getStringValue()).thenReturn("");

        Object result = converter.convertToJavaData(readCellData, contentProperty, null);
        assertEquals("", result);

        when(readCellData.getStringValue()).thenReturn(null);
        result = converter.convertToJavaData(readCellData, contentProperty, null);
        assertEquals("", result);
    }

    @Test
    void testConvertToJavaData_UnmatchedLabel() {
        try (MockedStatic<DictFrameworkUtils> utils = mockStatic(DictFrameworkUtils.class)) {
            utils.when(() -> DictFrameworkUtils.parseDictDataValue("testDict", "未知")).thenReturn(null);

            ReadCellData readCellData = mock(ReadCellData.class);
            when(readCellData.getStringValue()).thenReturn("未知");

            Object result = converter.convertToJavaData(readCellData, contentProperty, null);
            assertEquals("", result); // 未匹配返回空字符串（会记录 error 日志）
        }
    }

    // 模拟带注解的类
    static class TestClass {
        @cn.iocoder.yudao.framework.excel.core.annotations.DictFormat("testDict")
        private String dictField;
    }
}