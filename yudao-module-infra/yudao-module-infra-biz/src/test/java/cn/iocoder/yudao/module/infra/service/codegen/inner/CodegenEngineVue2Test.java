package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO @puhui999：单测需要 fix
/**
 * {@link CodegenEngine} 的 Vue2 + Element UI 单元测试
 *
 * @author 芋道源码
 */
@Disabled
public class CodegenEngineVue2Test extends CodegenEngineAbstractTest {

    @Test
    public void testExecute_vue2_one() {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE2.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns, null, null);
        // 断言
        assertResult(result, "codegen/vue2_one");
//        writeResult(result, "/root/ruoyi-vue-pro/yudao-module-infra/yudao-module-infra-biz/src/test/resources/codegen/vue2_one");
    }

    @Test
    public void testExecute_vue2_tree() {
        // 准备参数
        CodegenTableDO table = getTable("category")
                .setFrontType(CodegenFrontTypeEnum.VUE2.getType())
                .setTemplateType(CodegenTemplateTypeEnum.TREE.getType());
        List<CodegenColumnDO> columns = getColumnList("category");

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns, null, null);
        // 断言
        assertResult(result, "codegen/vue2_tree");
//        writeResult(result, "/root/ruoyi-vue-pro/yudao-module-infra/yudao-module-infra-biz/src/test/resources/codegen/vue2_tree");
//        writeFile(result, "/Users/yunai/test/demo66.zip");
    }

    @Test
    public void testExecute_vue2_master_normal() {
        testExecute_vue2_master(CodegenTemplateTypeEnum.MASTER_NORMAL, "codegen/vue2_master_normal");
    }

    @Test
    public void testExecute_vue2_master_erp() {
        testExecute_vue2_master(CodegenTemplateTypeEnum.MASTER_ERP, "codegen/vue2_master_erp");
    }

    @Test
    public void testExecute_vue2_master_inner() {
        testExecute_vue2_master(CodegenTemplateTypeEnum.MASTER_INNER, "codegen/vue2_master_inner");
    }

    private void testExecute_vue2_master(CodegenTemplateTypeEnum templateType,
                                         String path) {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE2.getType())
                .setTemplateType(templateType.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        // 准备参数（子表）
        CodegenTableDO contactTable = getTable("contact")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE2.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        List<CodegenColumnDO> contactColumns = getColumnList("contact");
        // 准备参数（班主任）
        CodegenTableDO teacherTable = getTable("teacher")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE2.getType())
                .setSubJoinColumnId(200L).setSubJoinMany(false);
        List<CodegenColumnDO> teacherColumns = getColumnList("teacher");

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns,
                Arrays.asList(contactTable, teacherTable), Arrays.asList(contactColumns, teacherColumns));
        // 断言
        assertResult(result, path);
//        writeResult(result, "/root/ruoyi-vue-pro/yudao-module-infra/yudao-module-infra-biz/src/test/resources/" + path);
//        writeFile(result, "/Users/yunai/test/demo11.zip");
    }

}
