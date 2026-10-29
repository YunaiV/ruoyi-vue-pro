package cn.iocoder.yudao.module.infra.service.db;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.type.TypeRegistry;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(DatabaseTableServiceImpl.class)
public class DatabaseTableServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DatabaseTableServiceImpl databaseTableService;

    @MockBean
    private DataSourceConfigService dataSourceConfigService;

    @Test
    public void testGetTableList() {
        // 准备参数
        Long dataSourceConfigId = randomLongId();
        // mock 方法
        DataSourceConfigDO dataSourceConfig = new DataSourceConfigDO().setUsername("sa").setPassword("")
                .setUrl("jdbc:h2:mem:testdb");
        when(dataSourceConfigService.getDataSourceConfig(eq(dataSourceConfigId)))
                .thenReturn(dataSourceConfig);

        // 调用
        List<TableInfo> tables = databaseTableService.getTableList(dataSourceConfigId,
                "config", "参数");
        // 断言
        assertEquals(1, tables.size());
        assertTableInfo(tables.get(0));
    }

    @Test
    public void testGetTable() {
        // 准备参数
        Long dataSourceConfigId = randomLongId();
        // mock 方法
        DataSourceConfigDO dataSourceConfig = new DataSourceConfigDO().setUsername("sa").setPassword("")
                .setUrl("jdbc:h2:mem:testdb");
        when(dataSourceConfigService.getDataSourceConfig(eq(dataSourceConfigId)))
                .thenReturn(dataSourceConfig);

        // 调用
        TableInfo tableInfo = databaseTableService.getTable(dataSourceConfigId, "infra_config");
        // 断言
        assertTableInfo(tableInfo);
    }

    @Test
    public void testOscarTypeConvertHandler() {
        TypeRegistry typeRegistry = new TypeRegistry(new GlobalConfig.Builder().build());
        // 神通的 TEXT 类型，JDBC 类型为 OTHER，按 String 处理
        assertEquals(DbColumnType.STRING, DatabaseTableServiceImpl.OSCAR_TYPE_CONVERT_HANDLER
                .convert(null, typeRegistry, buildMetaInfo(JdbcType.OTHER, "TEXT")));
        // 其它 OTHER 类型，保持 MyBatis Plus Generator 默认的转换
        assertEquals(DbColumnType.OBJECT, DatabaseTableServiceImpl.OSCAR_TYPE_CONVERT_HANDLER
                .convert(null, typeRegistry, buildMetaInfo(JdbcType.OTHER, "JSON")));
        assertEquals(DbColumnType.LONG, DatabaseTableServiceImpl.OSCAR_TYPE_CONVERT_HANDLER
                .convert(null, typeRegistry, buildMetaInfo(JdbcType.BIGINT, "BIGINT")));
    }

    private static TableField.MetaInfo buildMetaInfo(JdbcType jdbcType, String typeName) {
        TableField.MetaInfo metaInfo = new TableField.MetaInfo(null, null);
        metaInfo.setJdbcType(jdbcType);
        metaInfo.setTypeName(typeName);
        return metaInfo;
    }

    private void assertTableInfo(TableInfo tableInfo) {
        assertEquals("infra_config", tableInfo.getName());
        assertEquals("参数配置表", tableInfo.getComment());
        assertEquals(13, tableInfo.getFields().size());
        // id 字段
        TableField idField = tableInfo.getFields().get(0);
        assertEquals("id", idField.getName());
        assertEquals(JdbcType.BIGINT, idField.getMetaInfo().getJdbcType());
        assertEquals("编号", idField.getComment());
        assertFalse(idField.getMetaInfo().isNullable());
        assertTrue(idField.isKeyFlag());
        assertTrue(idField.isKeyIdentityFlag());
        assertEquals(DbColumnType.LONG, idField.getColumnType());
        assertEquals("id", idField.getPropertyName());
        // name 字段
        TableField nameField = tableInfo.getFields().get(3);
        assertEquals("name", nameField.getName());
        assertEquals(JdbcType.VARCHAR, nameField.getMetaInfo().getJdbcType());
        assertEquals("名字", nameField.getComment());
        assertFalse(nameField.getMetaInfo().isNullable());
        assertFalse(nameField.isKeyFlag());
        assertFalse(nameField.isKeyIdentityFlag());
        assertEquals(DbColumnType.STRING, nameField.getColumnType());
        assertEquals("name", nameField.getPropertyName());
    }
}
