package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.iocoder.dashboard.TestApplication;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolCodegenColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolCodegenTableMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ToolCodegenEngineTest {

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
