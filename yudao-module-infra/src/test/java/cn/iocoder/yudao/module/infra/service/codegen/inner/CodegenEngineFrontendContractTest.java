package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CodegenEngine} 的前端组件契约回归测试
 *
 * @author 芋道源码
 */
public class CodegenEngineFrontendContractTest extends CodegenEngineAbstractTest {

    @ParameterizedTest
    @EnumSource(value = CodegenFrontTypeEnum.class, names = {
            "VUE3_VBEN5_ANTD_SCHEMA", "VUE3_VBEN5_ANTDV_NEXT_SCHEMA", "VUE3_VBEN5_EP_SCHEMA"})
    public void testExecute_dictionaryControls(CodegenFrontTypeEnum frontType) {
        // 准备参数：同一字典的多选、单选和数字必填字段
        CodegenTableDO table = getTable("student").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        columns.add(new CodegenColumnDO().setJavaField("tags").setJavaType("String")
                .setColumnComment("标签").setDictType("system_user_sex").setHtmlType("checkbox")
                .setCreateOperation(true).setUpdateOperation(true).setNullable(false).setPrimaryKey(false));
        columns.add(new CodegenColumnDO().setJavaField("status").setJavaType("Integer")
                .setColumnComment("状态").setDictType("system_user_sex").setHtmlType("radio")
                .setCreateOperation(true).setUpdateOperation(true).setNullable(false).setPrimaryKey(false));
        columns.add(new CodegenColumnDO().setJavaField("amount").setJavaType("Integer")
                .setColumnComment("金额").setHtmlType("inputNumber").setDictType("")
                .setCreateOperation(true).setUpdateOperation(true).setNullable(false).setPrimaryKey(false));

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 调试用：导出真实生成结果，供前端组件验证
        String outputPath = System.getProperty("codegen.contractOutput");
        if (outputPath != null) {
            writeResult(result, outputPath + "/" + frontType.name());
        }
        // 断言：组选项不能变成 boolean Checkbox，必填数字不能被 Zod 推导为 null
        String data = findFile(result, "/data.ts");
        assertTrue(data.contains("component: 'CheckboxGroup'"));
        assertFalse(data.contains("component: 'Checkbox',"));
        assertTrue(data.contains("rules: 'required'"));
        assertFalse(data.contains("z.number("));
        if (frontType == CodegenFrontTypeEnum.VUE3_VBEN5_EP_SCHEMA) {
            assertTrue(data.contains("isButton: true"));
            assertFalse(data.contains("buttonStyle:"));
            assertFalse(data.contains("optionType:"));
            assertFalse(data.contains("allowClear:"));
        } else {
            assertTrue(data.contains("optionType: 'button'"));
            assertFalse(data.contains("clearable:"));
        }
    }

    @Test
    public void testExecute_eleTreeParentSelectable() {
        // 准备参数
        CodegenTableDO table = getTable("category").setFrontType(CodegenFrontTypeEnum.VUE3_VBEN5_EP_SCHEMA.getType())
                .setTemplateType(CodegenTemplateTypeEnum.TREE.getType());
        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("category"), null, null);
        // 断言：父节点可作为上级，使用 Element Plus 属性
        String data = findFile(result, "/data.ts");
        assertTrue(data.contains("defaultExpandAll: true"));
        assertTrue(data.contains("checkStrictly: true"));
        assertFalse(data.contains("treeDefaultExpandAll"));
    }

    @ParameterizedTest
    @EnumSource(value = CodegenFrontTypeEnum.class, names = {
            "VUE3_VBEN5_ANTD_SCHEMA", "VUE3_VBEN5_ANTD_GENERAL",
            "VUE3_VBEN5_ANTDV_NEXT_SCHEMA", "VUE3_VBEN5_ANTDV_NEXT_GENERAL",
            "VUE3_VBEN5_EP_SCHEMA", "VUE3_VBEN5_EP_GENERAL"})
    public void testExecute_importRemovalClearsPayload(CodegenFrontTypeEnum frontType) {
        // 准备参数
        codegenProperties.setImportEnable(true);
        codegenEngine.initGlobalBindingMap();
        CodegenTableDO table = getTable("student").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"), null, null);
        // 断言：移除可视文件时也清空提交来源
        String form = findFile(result, "/import-form.vue");
        assertTrue(form.contains(":on-remove=\"handleRemove\""));
        assertTrue(form.contains("formApi.setFieldValue('file', undefined)") || form.contains("fileRef.value = null"));
        if (frontType == CodegenFrontTypeEnum.VUE3_VBEN5_EP_SCHEMA
                || frontType == CodegenFrontTypeEnum.VUE3_VBEN5_EP_GENERAL) {
            assertTrue(form.contains(":on-exceed=\"handleExceed\""));
        }
    }

    @ParameterizedTest
    @EnumSource(value = CodegenFrontTypeEnum.class, names = {
            "VUE3_VBEN5_ANTD_SCHEMA", "VUE3_VBEN5_ANTD_GENERAL",
            "VUE3_VBEN5_ANTDV_NEXT_SCHEMA", "VUE3_VBEN5_ANTDV_NEXT_GENERAL",
            "VUE3_VBEN5_EP_SCHEMA", "VUE3_VBEN5_EP_GENERAL"})
    public void testExecute_masterChildEditing(CodegenFrontTypeEnum frontType) {
        // 准备参数：一对多联系人和一对一班主任
        CodegenTableDO table = getTable("student").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.MASTER_NORMAL.getType());
        CodegenTableDO contact = getTable("contact").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType()).setSubJoinColumnId(100L).setSubJoinMany(true);
        CodegenTableDO teacher = getTable("teacher").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType()).setSubJoinColumnId(200L).setSubJoinMany(false);
        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"),
                List.of(contact, teacher), List.of(getColumnList("contact"), getColumnList("teacher")));
        // 断言：新增和删除以当前表格为准，不重复追加，新增临时编号不提交
        String form = findFile(result, "/student-contact-form.vue");
        assertTrue(form.contains("getTableData()"));
        assertTrue(form.contains("inserted.has(row) ? { ...row, id: undefined } : { ...row }"));
        assertFalse(form.contains("getRemoveRecords()"));
        assertFalse(form.contains(".concat(insertRecords"));
        assertFalse(form.contains("$dictType"));
        assertTrue(form.contains("getStudentContactListByStudentId"));
        if (frontType.name().endsWith("SCHEMA")) {
            assertTrue(form.contains("<template #actions="));
        }
        result.forEach((path, content) -> {
            if (path.endsWith(".vue") || path.endsWith("data.ts")) {
                assertFalse(content.contains("#/api/infra/student"), path + "：API 导入必须匹配生成目录");
                assertFalse(content.contains("VxeTableGridOptions<StudentApi.StudentContact>") && content.contains("import type { InfraStudentApi }"),
                        path + "：子表命名空间应与导入一致");
            }
        });
        if (frontType == CodegenFrontTypeEnum.VUE3_VBEN5_ANTDV_NEXT_GENERAL) {
            assertTrue(form.matches("(?s).*import\\s*\\{[^}]*\\bmessage\\b[^}]*}\\s*from 'antdv-next'.*"),
                    "子表校验提示必须导入实际的消息组件");
        }
        if (frontType == CodegenFrontTypeEnum.VUE3_VBEN5_EP_GENERAL) {
            assertTrue(form.contains(":icon=\"Plus\""));
            assertFalse(form.contains("h(Plus)"), "图标使用导入的组件，避免未绑定的 h");
        }
        assertTrue(form.contains("function validate(): boolean"));
        assertTrue(form.contains("=== undefined"));
        assertTrue(form.contains("loadData([])") || form.contains("list.value = []"));
        String parentForm = findFile(result, "/modules/form.vue");
        assertTrue(parentForm.contains("studentContactFormRef.value?.validate()"));
        assertFalse(parentForm.contains("TODO 列表值校验"));
    }

    @ParameterizedTest
    @EnumSource(value = CodegenTemplateTypeEnum.class, names = { "MASTER_ERP", "MASTER_INNER" })
    public void testExecute_eleChildListImports(CodegenTemplateTypeEnum templateType) {
        // 准备参数
        CodegenFrontTypeEnum frontType = CodegenFrontTypeEnum.VUE3_VBEN5_EP_GENERAL;
        CodegenTableDO table = getTable("student").setFrontType(frontType.getType())
                .setTemplateType(templateType.getType());
        CodegenTableDO contact = getTable("contact").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType()).setSubJoinColumnId(100L).setSubJoinMany(true);
        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"),
                List.of(contact), List.of(getColumnList("contact")));
        // 断言：ERP 子表查询实际调用的工具必须导入
        String index = findFile(result, "/index.vue");
        assertTrue(index.contains("<VbenVxeTableToolbar"));
        assertTrue(index.contains("</VbenVxeTableToolbar>"));
        assertFalse(index.contains("<TableToolbar"));
        String list = findFile(result, "/student-contact-list.vue");
        if (templateType == CodegenTemplateTypeEnum.MASTER_ERP) {
            assertTrue(list.contains("<VbenVxeTableToolbar"));
            assertTrue(list.contains("</VbenVxeTableToolbar>"));
            assertFalse(list.contains("</TableToolbar>"));
            assertTrue(list.contains("cloneDeep(queryParams)"));
            assertTrue(list.matches("(?s).*import\\s*\\{[^}]*\\bcloneDeep\\b[^}]*}\\s*from '@vben/utils'.*"));
        }
        assertTrue(list.contains("formatDateTime(row."));
        assertTrue(list.matches("(?s).*import\\s*\\{[^}]*\\bformatDateTime\\b[^}]*}\\s*from '@vben/utils'.*"));
    }

    @Test
    public void testExecute_vue3ImportUploadResponse() {
        // 准备参数
        codegenProperties.setImportEnable(true);
        codegenEngine.initGlobalBindingMap();
        CodegenTableDO table = getTable("student").setFrontType(CodegenFrontTypeEnum.VUE3_ELEMENT_PLUS.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"), null, null);
        // 断言
        String form = findFile(result, "/StudentImportForm.vue");
        assertTrue(form.contains("const res = await StudentApi.importStudent(formData)"));
        assertTrue(form.contains("const data = res.data"));
    }


    @Test
    public void testExecute_vben2DateRangeFormat() {
        // 准备参数
        CodegenTableDO table = getTable("student").setFrontType(CodegenFrontTypeEnum.VUE3_VBEN2_ANTD_SCHEMA.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"), null, null);
        // 断言：查询区间提交后端需要的时间字符串
        String data = findFile(result, ".data.ts");
        assertTrue(data.contains("valueFormat: 'YYYY-MM-DD HH:mm:ss'"));
        assertTrue(data.contains("allowClear: true"));
    }

    @Test
    public void testExecute_uniappPaginationContract() {
        // 准备参数
        CodegenTableDO table = getTable("student").setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"), null, null);
        // 断言：移动端已有分页/失败恢复契约保持有效，避免机械套用 PC 组件写法
        String api = findFile(result, "/index.ts");
        assertTrue(api.contains("params: PageParam"));
        assertFalse(api.contains("PageReqVO"));
        String list = result.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("/index.vue") && entry.getValue().contains("<z-paging"))
                .findFirst().orElseThrow().getValue();
        assertTrue(list.contains("completeByTotal(data.list, data.total)"));
        assertTrue(list.contains("complete(false)"));
        assertTrue(list.contains("uni.$off("));
    }

    private static String findFile(Map<String, String> result, String suffix) {
        return result.entrySet().stream().filter(entry -> entry.getKey().endsWith(suffix))
                .findFirst().orElseThrow(() -> new AssertionError("缺少生成文件：" + suffix)).getValue();
    }
}
