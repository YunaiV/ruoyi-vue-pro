package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CodegenEngine} 的 Vue3 + Vben5 + Antdv Next 单元测试
 *
 * @author 芋道源码
 */
public class CodegenEngineVben5AntdvNextTest extends CodegenEngineAbstractTest {

    private static final Pattern ANTDV_NEXT_VALUE_IMPORT_PATTERN = Pattern.compile(
            "import\\s*\\{([^}]*)}\\s*from\\s*'antdv-next';?", Pattern.DOTALL);
    private static final Pattern RANGE_PICKER_COMPONENT_PATTERN = Pattern.compile("<RangePicker\\b");
    private static final Pattern RANGE_PICKER_HELPER_BINDING_PATTERN = Pattern.compile(
            "\\bv-bind\\s*=\\s*([\"'])\\s*getRangePickerDefaultProps\\s*\\(\\s*\\)\\s*\\1");
    private static final Pattern RANGE_PICKER_HELPER_CALL_PATTERN = Pattern.compile(
            "\\bgetRangePickerDefaultProps\\s*\\(\\s*\\)");
    private static final Pattern RANGE_PICKER_HELPER_IMPORT_PATTERN = Pattern.compile(
            "import\\s*\\{[^}]*\\bgetRangePickerDefaultProps\\b[^}]*}"
                    + "\\s*from\\s*'#/utils(?:/rangePickerProps)?';?", Pattern.DOTALL);
    private static final Pattern SCHEMA_RANGE_PICKER_COMPONENT_PATTERN = Pattern.compile(
            "\\bcomponent\\s*:\\s*([\"'])RangePicker\\1");
    private static final Pattern SCHEMA_RANGE_PICKER_HELPER_SPREAD_PATTERN = Pattern.compile(
            "\\.\\.\\.\\s*getRangePickerDefaultProps\\s*\\(\\s*\\)");

    private static final String IMPORT_FORM_PATH =
            "yudao-ui-admin-vben/src/views/infra/demo/modules/import-form.vue";
    private static final String SCHEMA_DATA_PATH =
            "yudao-ui-admin-vben/src/views/infra/demo/data.ts";
    private static final String STUDENT_CONTACT_FORM_PATH =
            "yudao-ui-admin-vben/src/views/infra/demo/modules/student-contact-form.vue";
    private static final String STUDENT_TEACHER_FORM_PATH =
            "yudao-ui-admin-vben/src/views/infra/demo/modules/student-teacher-form.vue";

    private static final List<String> INNER_TEMPLATE_PATHS = Arrays.asList(
            "codegen/vue3_vben5_antdv_next/general/views/modules/form_sub_inner.vue.vm",
            "codegen/vue3_vben5_antdv_next/general/views/modules/list_sub_inner.vue.vm",
            "codegen/vue3_vben5_antdv_next/schema/views/modules/form_sub_inner.vue.vm",
            "codegen/vue3_vben5_antdv_next/schema/views/modules/list_sub_inner.vue.vm");

    @Test
    public void testExecute_schemaMasterNormal() {
        // 调用
        Map<String, String> result = executeMaster(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_SCHEMA.getType(),
                CodegenTemplateTypeEnum.MASTER_NORMAL);

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        assertSchemaRangePickerGenerated(result);
        assertTabsGenerated(result);
    }

    @Test
    public void testExecute_schemaMasterErp() {
        // 调用
        Map<String, String> result = executeMaster(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_SCHEMA.getType(),
                CodegenTemplateTypeEnum.MASTER_ERP);

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        assertSchemaRangePickerGenerated(result);
        assertTabsGenerated(result);
    }

    @Test
    public void testExecute_schemaMasterInner() {
        // 调用
        Map<String, String> result = executeMaster(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_SCHEMA.getType(),
                CodegenTemplateTypeEnum.MASTER_INNER);

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        assertSchemaRangePickerGenerated(result);
        assertTabsGenerated(result);
    }

    @Test
    public void testExecute_generalTree() {
        // 调用
        Map<String, String> result = executeTree(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL.getType());

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        assertRuleGenerated(result);
        assertComponentGenerated(result, "FormItem");
        assertComponentGenerated(result, "TreeSelect");
    }

    @Test
    public void testExecute_generalMasterNormal() {
        // 调用
        Map<String, String> result = executeMaster(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL.getType(),
                CodegenTemplateTypeEnum.MASTER_NORMAL);

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        assertRuleGenerated(result);
        assertGeneralCrudComponentsGenerated(result);
        assertTabsGenerated(result);
    }

    @Test
    public void testExecute_generalMasterNormal_oneToOneJoinColumnOnlySelect() {
        // 调用
        Map<String, String> result = executeGeneralMasterNormalWithOneToOneJoinSelect();

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        String content = result.get(STUDENT_TEACHER_FORM_PATH);
        assertNotNull(content, STUDENT_TEACHER_FORM_PATH + "：未生成一对一子表表单");
        assertTrue(content.contains("<Select v-model:value=\"formData.studentId\""),
                STUDENT_TEACHER_FORM_PATH + "：应渲染 join 列的 Select 组件");
        assertTrue(content.contains("<SelectOption"), STUDENT_TEACHER_FORM_PATH + "：应渲染 SelectOption 组件");
        assertRootValueImport(STUDENT_TEACHER_FORM_PATH, content, "\\bSelectOption\\b");
    }

    @Test
    public void testExecute_generalMasterNormal_oneToManyJoinColumnOnlySelect() {
        // 调用
        Map<String, String> result = executeGeneralMasterNormalWithOneToManyJoinSelect();

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        String content = result.get(STUDENT_CONTACT_FORM_PATH);
        assertNotNull(content, STUDENT_CONTACT_FORM_PATH + "：未生成一对多子表表单");
        assertTrue(content.contains("<VxeTable"), STUDENT_CONTACT_FORM_PATH + "：应生成一对多子表表格");
        assertFalse(content.contains("<VxeColumn field=\"studentId\""),
                STUDENT_CONTACT_FORM_PATH + "：不应生成 join 列");
        assertFalse(content.contains("<Select v-model:value=\"row.studentId\""),
                STUDENT_CONTACT_FORM_PATH + "：不应渲染 join 列的 Select 组件");
        assertFalse(content.contains("<SelectOption"), STUDENT_CONTACT_FORM_PATH + "：不应渲染 SelectOption 组件");
        assertFalse(createRootImportPattern("import\\s*\\{", "\\bSelectOption\\b").matcher(content).find(),
                STUDENT_CONTACT_FORM_PATH + "：不应导入 SelectOption 组件");
    }

    @Test
    public void testExecute_generalMasterInner() {
        // 调用
        Map<String, String> result = executeMaster(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL.getType(),
                CodegenTemplateTypeEnum.MASTER_INNER);

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        assertRuleGenerated(result);
        assertGeneralCrudComponentsGenerated(result);
        assertTabsGenerated(result);
    }

    @Test
    public void testExecute_generalMasterErp() {
        // 调用
        Map<String, String> result = executeMaster(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL.getType(),
                CodegenTemplateTypeEnum.MASTER_ERP);

        // 断言
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        assertRuleGenerated(result);
        assertGeneralCrudComponentsGenerated(result);
        assertTabsGenerated(result);
    }

    @Test
    public void testExecute_oneImportEnable() {
        // 开启 import 开关
        codegenProperties.setImportEnable(true);
        codegenEngine.initGlobalBindingMap();

        // 调用并断言
        assertImportFormCompatibleWithAntdvNext(
                executeOne(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_SCHEMA.getType()));
        assertImportFormCompatibleWithAntdvNext(
                executeOne(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL.getType()));
    }

    @Test
    public void testInnerTemplatesParseAntdvNext() {
        INNER_TEMPLATE_PATHS.forEach(path -> {
            String template = ResourceUtil.readUtf8Str(path);
            assertFalse(template.contains("codegen/vue3_vben5_antd/"), path + "：不应引用 Ant Design Vue 模板");
            assertTrue(template.contains("codegen/vue3_vben5_antdv_next/"), path + "：应引用 Antdv Next 模板");
        });
    }

    private Map<String, String> executeOne(Integer frontType) {
        CodegenTableDO table = getTable("student")
                .setFrontType(frontType)
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        return codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"), null, null);
    }

    private Map<String, String> executeTree(Integer frontType) {
        CodegenTableDO table = getTable("category")
                .setFrontType(frontType)
                .setTemplateType(CodegenTemplateTypeEnum.TREE.getType());
        return codegenEngine.execute(DbType.MYSQL, table, getColumnList("category"), null, null);
    }

    private Map<String, String> executeGeneralMasterNormalWithOneToOneJoinSelect() {
        return executeGeneralMasterNormalWithJoinSelect("teacher", 200L, false);
    }

    private Map<String, String> executeGeneralMasterNormalWithOneToManyJoinSelect() {
        return executeGeneralMasterNormalWithJoinSelect("contact", 100L, true);
    }

    private Map<String, String> executeGeneralMasterNormalWithJoinSelect(String subTableName,
                                                                         Long subJoinColumnId,
                                                                         boolean subJoinMany) {
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL.getType())
                .setTemplateType(CodegenTemplateTypeEnum.MASTER_NORMAL.getType());
        CodegenTableDO subTable = getTable(subTableName)
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL.getType())
                .setSubJoinColumnId(subJoinColumnId).setSubJoinMany(subJoinMany);
        List<CodegenColumnDO> subColumns = getColumnList(subTableName);
        // 稀疏场景：join 列是唯一的 select，避免其它 select 掩盖 join 列对导入判定的影响。
        subColumns.forEach(column -> {
            if (subJoinColumnId.equals(column.getId())) {
                column.setHtmlType("select");
                column.setDictType("");
            } else if ("select".equals(column.getHtmlType())) {
                column.setHtmlType("input");
            }
        });
        return codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"),
                List.of(subTable), List.of(subColumns));
    }

    private Map<String, String> executeMaster(Integer frontType, CodegenTemplateTypeEnum templateType) {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(frontType)
                .setTemplateType(templateType.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        // 准备参数（子表）
        CodegenTableDO contactTable = getTable("contact")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(frontType)
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        List<CodegenColumnDO> contactColumns = getColumnList("contact");
        // 准备参数（班主任）
        CodegenTableDO teacherTable = getTable("teacher")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(frontType)
                .setSubJoinColumnId(200L).setSubJoinMany(false);
        List<CodegenColumnDO> teacherColumns = getColumnList("teacher");

        // 调用
        return codegenEngine.execute(DbType.MYSQL, table, columns,
                Arrays.asList(contactTable, teacherTable), Arrays.asList(contactColumns, teacherColumns));
    }

    private static void assertGeneratedCodeCompatibleWithAntdvNext(Map<String, String> result) {
        assertTrue(result.values().stream().anyMatch(content -> content.contains("from 'antdv-next")),
                "生成结果应使用 antdv-next");
        result.forEach((filePath, content) -> {
            assertFalse(content.contains("ant-design-vue"), filePath + "：不应使用 ant-design-vue");
            assertFalse(content.contains("antdv-next/es/"), filePath + "：不应使用未导出的 antdv-next 深层路径");
            assertFalse(content.contains("<Form.Item"), filePath + "：不应使用 Form.Item");
            assertFalse(content.contains("</Form.Item>"), filePath + "：不应使用 Form.Item");
            assertRootValueImportsSorted(filePath, content);
            assertRootValueImportMatchesUsage(filePath, content,
                    "\\bDateRangePicker\\s+as\\s+RangePicker\\b", "<RangePicker\\b", "RangePicker");
            assertRootValueImportMatchesUsage(filePath, content,
                    "\\bFormItem\\b", "<FormItem\\b", "FormItem");
            assertRootValueImportMatchesUsage(filePath, content,
                    "\\bSelectOption\\b", "<SelectOption\\b", "SelectOption");
            assertRootValueImportMatchesUsage(filePath, content,
                    "\\bTabPane\\b", "<TabPane\\b", "TabPane");
            assertRootValueImportMatchesUsage(filePath, content,
                    "\\bTabs\\b", "<Tabs\\b", "Tabs");
            assertRootTypeImportMatchesUsage(filePath, content,
                    "\\bRule\\b", "\\bRule\\b", "Rule");
            assertRangePickerHelperImportMatchesUsage(filePath, content);
            if (filePath.endsWith(".vue")) {
                assertRangePickerHelperBindingMatchesComponent(filePath, content);
            } else if (filePath.endsWith(".ts")) {
                assertSchemaRangePickerHelperMatchesComponent(filePath, content);
            }
        });
    }

    private static void assertImportFormCompatibleWithAntdvNext(Map<String, String> result) {
        assertGeneratedCodeCompatibleWithAntdvNext(result);
        String content = result.get(IMPORT_FORM_PATH);
        assertNotNull(content, IMPORT_FORM_PATH + "：未生成导入表单");
        assertTrue(content.contains("<Upload"), IMPORT_FORM_PATH + "：应生成 Upload 组件");
        assertRootValueImport(IMPORT_FORM_PATH, content, "\\bUpload\\b");
        assertTrue(content.contains("beforeUpload(file: File)"), IMPORT_FORM_PATH + "：上传参数应使用原生 File 类型");
        assertFalse(content.contains("UploadFile"), IMPORT_FORM_PATH + "：不应使用上传列表项类型 UploadFile");
        assertFalse(content.contains("as unknown as File"), IMPORT_FORM_PATH + "：不应通过双重断言转换上传文件");
        assertFalse(content.contains("FileType"), IMPORT_FORM_PATH + "：不应继续使用旧 FileType");
    }

    private static void assertTabsGenerated(Map<String, String> result) {
        assertComponentGenerated(result, "Tabs");
        assertComponentGenerated(result, "TabPane");
    }

    private static void assertGeneralCrudComponentsGenerated(Map<String, String> result) {
        assertComponentGenerated(result, "FormItem");
        assertComponentGenerated(result, "RangePicker");
        assertComponentGenerated(result, "SelectOption");
    }

    private static void assertRuleGenerated(Map<String, String> result) {
        assertTrue(result.values().stream().map(CodegenEngineVben5AntdvNextTest::removeAntdvNextRootImports)
                        .anyMatch(content -> Pattern.compile("\\bRule\\b").matcher(content).find()),
                "生成结果应包含 Rule 类型校验规则");
    }

    private static void assertSchemaRangePickerGenerated(Map<String, String> result) {
        String content = result.get(SCHEMA_DATA_PATH);
        assertNotNull(content, SCHEMA_DATA_PATH + "：未生成 Schema 配置");
        long componentCount = countMatches(SCHEMA_RANGE_PICKER_COMPONENT_PATTERN, content);
        assertTrue(componentCount > 0, SCHEMA_DATA_PATH + "：应生成 RangePicker 组件配置");
    }

    private static void assertComponentGenerated(Map<String, String> result, String component) {
        assertTrue(result.values().stream().anyMatch(content -> content.contains("<" + component)),
                "生成结果应包含 " + component + " 组件");
    }

    private static void assertRootValueImportMatchesUsage(String filePath, String content,
                                                           String expectedImportRegex, String usageRegex,
                                                           String bindingName) {
        assertRootImportMatchesUsage(filePath, content, "import\\s*\\{", expectedImportRegex,
                usageRegex, bindingName);
    }

    private static void assertRootTypeImportMatchesUsage(String filePath, String content,
                                                          String expectedImportRegex, String usageRegex,
                                                          String bindingName) {
        assertRootImportMatchesUsage(filePath, content, "import\\s+type\\s*\\{", expectedImportRegex,
                usageRegex, bindingName);
    }

    private static void assertRootImportMatchesUsage(String filePath, String content, String importPrefix,
                                                      String expectedImportRegex, String usageRegex,
                                                      String bindingName) {
        boolean imported = createRootImportPattern(importPrefix, expectedImportRegex).matcher(content).find();
        boolean used = Pattern.compile(usageRegex).matcher(removeAntdvNextRootImports(content)).find();
        assertEquals(used, imported, filePath + "：" + bindingName + (used ? " 缺少合法根导入" : " 存在孤立根导入"));
    }

    private static String removeAntdvNextRootImports(String content) {
        Pattern pattern = Pattern.compile("import\\s+(?:type\\s+)?\\{[^}]*}\\s*from\\s*'antdv-next';?",
                Pattern.DOTALL);
        return pattern.matcher(content).replaceAll("");
    }

    private static void assertRootValueImport(String filePath, String content, String expectedRegex) {
        assertRootImport(filePath, content, "import\\s*\\{", expectedRegex);
    }

    private static void assertRootValueImportsSorted(String filePath, String content) {
        var matcher = ANTDV_NEXT_VALUE_IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            List<String> actual = Arrays.stream(matcher.group(1).split(","))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .map(item -> item.replaceFirst("^.*\\s+as\\s+", ""))
                    .toList();
            List<String> expected = actual.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
            assertEquals(expected, actual, filePath + "：antdv-next 命名导入未按 natural 顺序排列");
        }
    }

    private static void assertRangePickerHelperImportMatchesUsage(String filePath, String content) {
        boolean imported = RANGE_PICKER_HELPER_IMPORT_PATTERN.matcher(content).find();
        boolean called = RANGE_PICKER_HELPER_CALL_PATTERN.matcher(content).find();
        assertEquals(called, imported,
                filePath + "：getRangePickerDefaultProps() 调用与导入不一致");
    }

    private static void assertRangePickerHelperBindingMatchesComponent(String filePath, String content) {
        long componentCount = countMatches(RANGE_PICKER_COMPONENT_PATTERN, content);
        long bindingCount = countMatches(RANGE_PICKER_HELPER_BINDING_PATTERN, content);
        assertEquals(componentCount, bindingCount,
                filePath + "：RangePicker 与 getRangePickerDefaultProps() 绑定数量不一致");
    }

    private static void assertSchemaRangePickerHelperMatchesComponent(String filePath, String content) {
        long componentCount = countMatches(SCHEMA_RANGE_PICKER_COMPONENT_PATTERN, content);
        long helperSpreadCount = countMatches(SCHEMA_RANGE_PICKER_HELPER_SPREAD_PATTERN, content);
        assertEquals(componentCount, helperSpreadCount,
                filePath + "：Schema RangePicker 与 getRangePickerDefaultProps() 展开数量不一致");
    }

    private static long countMatches(Pattern pattern, String content) {
        return pattern.matcher(content).results().count();
    }

    private static void assertRootImport(String filePath, String content, String importPrefix, String expectedRegex) {
        Pattern pattern = createRootImportPattern(importPrefix, expectedRegex);
        assertTrue(pattern.matcher(content).find(), filePath + "：缺少合法的 antdv-next 根导入 " + expectedRegex);
    }

    private static Pattern createRootImportPattern(String importPrefix, String expectedRegex) {
        return Pattern.compile(importPrefix + "[^}]*" + expectedRegex
                + "[^}]*}\\s*from\\s*'antdv-next';?", Pattern.DOTALL);
    }

}
