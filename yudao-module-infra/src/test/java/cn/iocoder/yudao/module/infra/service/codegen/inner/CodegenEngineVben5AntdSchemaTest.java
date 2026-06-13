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

/**
 * {@link CodegenEngine} 的 Vue3 + Vben5 + AntD + Schema 单元测试
 *
 * @author 芋道源码
 */
public class CodegenEngineVben5AntdSchemaTest extends CodegenEngineAbstractTest {

    private static final Integer FRONT_TYPE = CodegenFrontTypeEnum.VUE3_VBEN5_ANTD_SCHEMA.getType();

    @Test
    public void testExecute_one() {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(FRONT_TYPE)
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 断言
        assertResult(result, "/vben5_antd_schema_one");
    }

    @Test
    public void testExecute_one_importEnable() {
        // 开启 import 开关
        codegenProperties.setImportEnable(true);
        codegenEngine.initGlobalBindingMap();
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(FRONT_TYPE)
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 断言
        assertResult(result, "/vben5_antd_schema_one_importEnable");
    }

    @Test
    public void testExecute_tree() {
        // 准备参数
        CodegenTableDO table = getTable("category")
                .setFrontType(FRONT_TYPE)
                .setTemplateType(CodegenTemplateTypeEnum.TREE.getType());
        List<CodegenColumnDO> columns = getColumnList("category");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        // 断言
        assertResult(result, "/vben5_antd_schema_tree");
    }

    @Test
    public void testExecute_master_normal() {
        testExecute_master(CodegenTemplateTypeEnum.MASTER_NORMAL, "/vben5_antd_schema_master_normal");
    }

    @Test
    public void testExecute_master_erp() {
        testExecute_master(CodegenTemplateTypeEnum.MASTER_ERP, "/vben5_antd_schema_master_erp");
    }

    @Test
    public void testExecute_master_inner() {
        testExecute_master(CodegenTemplateTypeEnum.MASTER_INNER, "/vben5_antd_schema_master_inner");
    }

    private void testExecute_master(CodegenTemplateTypeEnum templateType, String path) {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(FRONT_TYPE)
                .setTemplateType(templateType.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        // 准备参数（子表）
        CodegenTableDO contactTable = getTable("contact")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(FRONT_TYPE)
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        List<CodegenColumnDO> contactColumns = getColumnList("contact");
        // 准备参数（班主任）
        CodegenTableDO teacherTable = getTable("teacher")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(FRONT_TYPE)
                .setSubJoinColumnId(200L).setSubJoinMany(false);
        List<CodegenColumnDO> teacherColumns = getColumnList("teacher");

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns,
                Arrays.asList(contactTable, teacherTable), Arrays.asList(contactColumns, teacherColumns));
        // 断言
        assertResult(result, path);
    }

}
