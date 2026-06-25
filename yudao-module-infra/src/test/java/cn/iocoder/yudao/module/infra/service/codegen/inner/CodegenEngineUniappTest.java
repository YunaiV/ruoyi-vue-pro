package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

}
