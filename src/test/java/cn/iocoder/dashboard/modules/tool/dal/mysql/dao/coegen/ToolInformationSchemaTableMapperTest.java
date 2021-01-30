package cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen;

import cn.iocoder.dashboard.TestApplication;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaTableDO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ToolInformationSchemaTableMapperTest {

    @Resource
    private ToolInformationSchemaTableMapper toolInformationSchemaTableMapper;

    @Test
    public void tstSelectListByTableSchema() {
        List<ToolInformationSchemaTableDO> tables = toolInformationSchemaTableMapper
                .selectListByTableSchema("ruoyi-vue-pro");
        assertTrue(tables.size() > 0);
    }

}
