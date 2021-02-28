package cn.iocoder.dashboard.modules.tool.dal.mysql.coegen;

import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaColumnDO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("unit-test")
public class ToolInformationSchemaColumnMapperTest {

    @Resource
    private ToolSchemaColumnMapper toolInformationSchemaColumnMapper;

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testSelectListByTableName() {
        List<ToolSchemaColumnDO> columns = toolInformationSchemaColumnMapper
                .selectListByTableName("inf_config");
        assertTrue(columns.size() > 0);
    }

}
