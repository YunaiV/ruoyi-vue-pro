package cn.iocoder.yudao.module.infra.dal.mysql.codegen;

import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaColumnDO;
import cn.iocoder.yudao.module.tool.test.BaseDbUnitTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaColumnMapperTest extends BaseDbUnitTest {

    @Resource
    private SchemaColumnMapper schemaColumnMapper;

    @Test
    public void testSelectListByTableName() {
        List<SchemaColumnDO> columns = schemaColumnMapper.selectListByTableName("", "inf_config");
        assertTrue(columns.size() > 0);
    }

}
