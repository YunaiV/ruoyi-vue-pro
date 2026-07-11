package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

}
