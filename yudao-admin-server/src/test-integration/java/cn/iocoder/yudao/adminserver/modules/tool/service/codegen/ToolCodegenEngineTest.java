package cn.iocoder.yudao.module.tool.service.codegen;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.yudao.module.tool.dal.mysql.codegen.ToolCodegenColumnMapper;
import cn.iocoder.yudao.module.tool.dal.mysql.codegen.ToolCodegenTableMapper;
import cn.iocoder.yudao.module.tool.service.codegen.impl.ToolCodegenEngine;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

public class ToolCodegenEngineTest extends BaseDbUnitTest {

    @Resource
    private ToolCodegenTableMapper codegenTableMapper;
    @Resource
    private ToolCodegenColumnMapper codegenColumnMapper;

    @Resource
    private ToolCodegenEngine codegenEngine;

    @Test
    public void testExecute() {
        ToolCodegenTableDO table = codegenTableMapper.selectById(20);
        List<ToolCodegenColumnDO> columns = codegenColumnMapper.selectListByTableId(table.getId());
        Map<String, String> result = codegenEngine.execute(table, columns);
        result.forEach((s, s2) -> System.out.println(s2));
//        System.out.println(result.get("vue/views/system/test/index.vue"));
    }

}
