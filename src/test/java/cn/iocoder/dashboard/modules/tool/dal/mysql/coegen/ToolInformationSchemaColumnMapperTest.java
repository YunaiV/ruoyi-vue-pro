package cn.iocoder.dashboard.modules.tool.dal.mysql.coegen;

import cn.iocoder.dashboard.TestApplication;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaColumnDO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ToolInformationSchemaColumnMapperTest {

    @Resource
    private ToolSchemaColumnMapper toolInformationSchemaColumnMapper;

    @Test
    public void testSelectListByTableName() {
        List<ToolSchemaColumnDO> columns = toolInformationSchemaColumnMapper
                .selectListByTableName("inf_config");
        assertTrue(columns.size() > 0);
    }

}
