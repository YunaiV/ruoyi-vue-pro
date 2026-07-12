package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CodegenEngine} 的 Vue3 Admin Uniapp + Wot UI 单元测试
 *
 * @author 芋道源码
 */
public class CodegenEngineUniappTest extends CodegenEngineAbstractTest {

    @Test
    public void testExecute_one() {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 断言
        assertResult(result, "/vue3_admin_uniapp_one");
    }

    @Test
    public void testExecute_dictSearch() {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");

        // 调用并断言数字、布尔字典
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        String searchForm = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/components/search-form.vue");
        assertFalse(searchForm.contains("<wd-radio"));
        assertTrue(searchForm.contains("all-option"));
        assertTrue(searchForm.contains(":columns=\"getIntDictOptions(DICT_TYPE.SYSTEM_USER_SEX)\""));
        assertTrue(searchForm.contains(":columns=\"getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)\""));

        // 调用并断言字符串字典
        columns.stream().filter(column -> "sex".equals(column.getJavaField())).findFirst().orElseThrow()
                .setJavaType("String");
        result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        searchForm = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/components/search-form.vue");
        assertTrue(searchForm.contains(":columns=\"getStrDictOptions(DICT_TYPE.SYSTEM_USER_SEX)\""));
        assertTrue(searchForm.contains("sex: undefined as string | undefined,"));
    }

    @Test
    public void testExecute_tree() {
        // 准备参数
        CodegenTableDO table = getTable("category")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.TREE.getType());
        List<CodegenColumnDO> columns = getColumnList("category");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 断言
        assertResult(result, "/vue3_admin_uniapp_tree");
    }

    @Test
    public void testExecute_semantics() {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 断言
        String api = result.get("yudao-ui-admin-uniapp/src/api/infra/demo/index.ts");
        assertTrue(api.contains("birthday: number"));
        assertTrue(api.contains("createTime?: number"));
        assertFalse(api.contains("createTime?: Date"));
        String detail = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/detail/index.vue");
        assertTrue(detail.contains("type=\"danger\""));
        assertFalse(detail.contains("type=\"error\""));
    }

    @Test
    public void testExecute_stringPrimaryKey() {
        // 准备主表
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.MASTER_ERP.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        columns.stream().filter(CodegenColumnDO::getPrimaryKey).findFirst().orElseThrow().setJavaType("String");
        // 准备子表
        CodegenTableDO contactTable = getTable("contact")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        List<CodegenColumnDO> contactColumns = getColumnList("contact");
        contactColumns.stream().filter(CodegenColumnDO::getPrimaryKey).findFirst().orElseThrow().setJavaType("String");
        contactColumns.stream().filter(column -> Objects.equals(column.getId(), 100L))
                .findFirst().orElseThrow().setJavaType("String");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns,
                List.of(contactTable), List.of(contactColumns));
        // 断言
        String api = result.get("yudao-ui-admin-uniapp/src/api/infra/demo/index.ts");
        assertTrue(api.contains("return http.post<string>('/infra/student/create', data)"));
        assertTrue(api.contains("studentId: string"));
        assertTrue(api.contains("getStudentContact(id: string)"));
        assertTrue(api.contains("deleteStudentContact(id: string)"));
        String detail = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/detail/index.vue");
        assertFalse(detail.contains("Number(props.id)"));
        String subForm = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/student-contact/form/index.vue");
        assertTrue(subForm.contains("id?: string"));
        assertTrue(subForm.contains("studentId: string"));
    }

    @Test
    public void testExecute_treeSearch() {
        // 准备参数
        CodegenTableDO table = getTable("category")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.TREE.getType());
        List<CodegenColumnDO> columns = getColumnList("category");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 断言
        String index = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/index.vue");
        assertTrue(index.contains("currentParentId.value === 0\n  ? list.value"));
        assertFalse(index.contains("list.value.filter"));
        String breadcrumb = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/components/breadcrumb.vue");
        assertTrue(breadcrumb.contains("modelValue: number"));
        assertFalse(breadcrumb.contains("TreeValue"));
    }

    @Test
    public void testExecute_duplicateSubPath() {
        // 准备主表
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(CodegenTemplateTypeEnum.MASTER_ERP.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        // 准备两个归一化名称相同的子表
        CodegenTableDO crmItemTable = getTable("contact").setModuleName("crm").setClassName("CrmOrderItem")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        CodegenTableDO erpItemTable = getTable("contact").setModuleName("erp").setClassName("ErpOrderItem")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        List<CodegenColumnDO> crmItemColumns = getColumnList("contact");
        List<CodegenColumnDO> erpItemColumns = getColumnList("contact");

        // 调用
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> codegenEngine.execute(DbType.MYSQL, table, columns,
                        Arrays.asList(crmItemTable, erpItemTable), Arrays.asList(crmItemColumns, erpItemColumns)));
        // 断言
        assertTrue(exception.getMessage().contains("生成文件路径重复"));
        assertTrue(exception.getMessage().contains("subIndex=0"));
        assertTrue(exception.getMessage().contains("subIndex=1"));
    }

    @Test
    public void testExecute_masterNormal() {
        testExecute_master(CodegenTemplateTypeEnum.MASTER_NORMAL, "/vue3_admin_uniapp_master_normal");
    }

    @Test
    public void testExecute_masterInner() {
        testExecute_master(CodegenTemplateTypeEnum.MASTER_INNER, "/vue3_admin_uniapp_master_inner");
    }

    @Test
    public void testExecute_masterErp() {
        testExecute_master(CodegenTemplateTypeEnum.MASTER_ERP, "/vue3_admin_uniapp_master_erp");
    }

    private void testExecute_master(CodegenTemplateTypeEnum templateType, String path) {
        // 准备主表
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setTemplateType(templateType.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        // 准备一对多子表
        CodegenTableDO contactTable = getTable("contact")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        List<CodegenColumnDO> contactColumns = getColumnList("contact");
        // 准备一对一子表
        CodegenTableDO teacherTable = getTable("teacher")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT.getType())
                .setSubJoinColumnId(200L).setSubJoinMany(false);
        List<CodegenColumnDO> teacherColumns = getColumnList("teacher");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns,
                Arrays.asList(contactTable, teacherTable), Arrays.asList(contactColumns, teacherColumns));
        // 断言
        assertResult(result, path);
        if (templateType != CodegenTemplateTypeEnum.MASTER_ERP) {
            String api = result.get("yudao-ui-admin-uniapp/src/api/infra/demo/index.ts");
            assertTrue(api.contains("studentTeacher?: StudentTeacher | null"));
            String form = result.get("yudao-ui-admin-uniapp/src/pages-infra/demo/form/index.vue");
            assertTrue(form.contains("ref<StudentTeacher | null>(null)"));
            assertTrue(form.contains("studentTeacher.value = studentTeacherData || null"));
            assertTrue(form.contains(":model-value=\"item.birthday || Date.now()\""));
            assertTrue(form.contains("v-model=\"item.sex\""));
            assertTrue(form.contains("<yd-upload-img v-model=\"item.avatar\""));
            assertTrue(form.contains("<yd-upload-file v-model=\"item.video\""));
            assertTrue(form.contains("<wd-textarea v-model=\"item.description\""));
            assertTrue(form.contains("function validateSubTables()"));
        } else {
            String contactForm = result.get(
                    "yudao-ui-admin-uniapp/src/pages-infra/demo/student-contact/form/index.vue");
            assertTrue(contactForm.contains(":model-value=\"formData.birthday || Date.now()\""));
            assertTrue(contactForm.contains("v-model=\"formData.sex\""));
            assertTrue(contactForm.contains("<yd-upload-img v-model=\"formData.avatar\""));
            assertTrue(contactForm.contains("<yd-upload-file v-model=\"formData.video\""));
            assertTrue(contactForm.contains("<wd-textarea v-model=\"formData.description\""));
        }
    }

}
