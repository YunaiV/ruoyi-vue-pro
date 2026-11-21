package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.iocoder.yudao.framework.excel.core.convert.JsonConvert;
import cn.iocoder.yudao.framework.excel.core.convert.MultiDictConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CodegenHelper} 的单元测试
 *
 * @author 芋道源码
 */
public class CodegenHelperTest {

    private final CodegenHelper codegenHelper = new CodegenHelper();

    @Test
    public void testIsCollection() {
        assertTrue(codegenHelper.isCollection(column("String", "checkbox")));
        assertTrue(codegenHelper.isCollection(column("Integer", "checkbox")));
        assertTrue(codegenHelper.isCollection(column("Long", "checkbox")));
        assertFalse(codegenHelper.isCollection(column("Boolean", "checkbox")));
        assertFalse(codegenHelper.isCollection(column("String", "select")));
        assertTrue(codegenHelper.isLongCollection(column("Long", "checkbox")));
        assertFalse(codegenHelper.isLongCollection(column("Integer", "checkbox")));
    }

    @Test
    public void testHasCollection() {
        CodegenColumnDO collection = column("String", "checkbox").setDictType("system_user_sex")
                .setListOperation(true).setListOperationResult(true).setCreateOperation(true);
        CodegenColumnDO scalar = column("String", "input");
        assertTrue(codegenHelper.hasCollection(List.of(scalar, collection)));
        assertFalse(codegenHelper.hasCollection(List.of(scalar)));
        assertTrue(codegenHelper.hasCollectionListCondition(List.of(scalar, collection)));
        assertFalse(codegenHelper.hasCollectionListCondition(List.of(column("String", "checkbox"))));
        assertTrue(codegenHelper.hasCollectionDictListResult(List.of(collection)));
        assertTrue(codegenHelper.hasSubCollectionDictListResult(List.of(List.of(scalar), List.of(collection))));
        // 表单中的集合字段
        assertEquals(List.of(collection), codegenHelper.getCollectionFormColumns(List.of(scalar, collection)));
        assertTrue(codegenHelper.hasCollectionFormColumn(List.of(collection)));
        assertFalse(codegenHelper.hasCollectionFormColumn(List.of(column("String", "checkbox"))));
        assertTrue(codegenHelper.hasSubCollectionFormColumn(List.of(List.of(scalar), List.of(collection))));
    }

    @Test
    public void testType() {
        assertEquals("List<String>", codegenHelper.getJavaType(column("String", "checkbox")));
        assertEquals("List<Long>", codegenHelper.getJavaType(column("Long", "checkbox")));
        assertEquals("String", codegenHelper.getJavaType(column("String", "input")));
        assertEquals("string[]", codegenHelper.getCollectionTsType(column("String", "checkbox")));
        assertEquals("number[]", codegenHelper.getCollectionTsType(column("Integer", "checkbox")));
        // Long：前端使用字符串表示元素，接口返回的安全范围内的 Long 仍为 number
        assertEquals("(number | string)[]", codegenHelper.getCollectionTsType(column("Long", "checkbox")));
    }

    @Test
    public void testExcel() {
        CodegenColumnDO dict = column("Integer", "checkbox").setDictType("system_user_sex")
                .setListOperationResult(true).setCreateOperation(true);
        CodegenColumnDO noDict = column("Long", "checkbox").setDictType("").setListOperationResult(true);
        // 设置字典：MultiDictConvert 双向转换；未设置字典：JsonConvert 仅导出
        assertEquals("MultiDictConvert", codegenHelper.getExcelConverter(dict));
        assertEquals("JsonConvert", codegenHelper.getExcelConverter(noDict));
        assertNull(codegenHelper.getExcelConverter(column("String", "input")));
        // 不同场景的转换器
        List<CodegenColumnDO> columns = List.of(dict, noDict, column("String", "input").setCreateOperation(true));
        assertEquals(List.of(JsonConvert.class.getName(), MultiDictConvert.class.getName()),
                codegenHelper.getExcelConverterClassNames(columns, CodegenHelper.EXCEL_SCENE_RESP));
        assertEquals(List.of(MultiDictConvert.class.getName()),
                codegenHelper.getExcelConverterClassNames(columns, CodegenHelper.EXCEL_SCENE_IMPORT));
    }

    @Test
    public void testFormImport() {
        CodegenColumnDO id = column("Long", "input").setPrimaryKey(true).setNullable(false).setUpdateOperation(true);
        CodegenColumnDO join = column("Long", "input").setNullable(false).setCreateOperation(true);
        CodegenColumnDO sex = column("Integer", "select").setDictType("system_user_sex").setNullable(true)
                .setCreateOperation(true);
        CodegenColumnDO birthday = column("LocalDateTime", "datetime").setNullable(true).setUpdateOperation(true);
        CodegenColumnDO readonly = column("String", "editor").setNullable(false);
        // 组件：主键不渲染 input；非表单字段不计入
        assertFalse(codegenHelper.hasFormHtmlType(List.of(id, readonly), "input"));
        assertFalse(codegenHelper.hasFormHtmlType(List.of(id, readonly), "editor"));
        assertTrue(codegenHelper.hasFormHtmlType(List.of(id, sex), "select"));
        // 字典、日期
        assertTrue(codegenHelper.hasFormDict(List.of(id, sex)));
        assertFalse(codegenHelper.hasFormDict(List.of(id, join)));
        assertTrue(codegenHelper.hasFormDateColumn(List.of(sex, birthday)));
        assertFalse(codegenHelper.hasFormDateColumn(List.of(sex)));
        assertTrue(codegenHelper.hasSubFormDateColumn(List.of(List.of(sex), List.of(birthday))));
        // 必填：主键、非表单字段不计入；排除子表的 join 字段后再判断
        assertTrue(codegenHelper.hasFormRequiredColumn(List.of(id, join, sex)));
        List<CodegenColumnDO> formColumns = codegenHelper.excludeColumn(List.of(id, join, sex, readonly), join);
        assertEquals(List.of(id, sex, readonly), formColumns);
        assertFalse(codegenHelper.hasFormRequiredColumn(formColumns));
    }

    private static CodegenColumnDO column(String javaType, String htmlType) {
        return new CodegenColumnDO().setJavaType(javaType).setHtmlType(htmlType).setPrimaryKey(false)
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(false).setListOperationResult(false);
    }

}
