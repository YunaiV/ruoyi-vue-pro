package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.iocoder.dashboard.TestApplication;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolCodegenColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolCodegenTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

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
        ToolCodegenTableDO table = codegenTableMapper.selectById(14);
        List<ToolCodegenColumnDO> columns = codegenColumnMapper.selectListByTableId(table.getId());
        codegenEngine.execute(table, columns);
    }

}
