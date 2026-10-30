package cn.iocoder.yudao.framework.mybatis.core.enums;

import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link DbTypeEnum} 的单元测试
 *
 * @author 芋道源码
 */
public class DbTypeEnumTest {

    @Test
    public void testFind() {
        // 华为 GaussDB 原生驱动（jdbc:gaussdb:）返回的 ProductName
        assertEquals(DbType.GAUSS_DB, DbTypeEnum.find("GaussDB"));
        // org.opengauss 的 openGauss JDBC 驱动（jdbc:opengauss:）连接 MogDB 时返回的 ProductName
        assertEquals(DbType.OPENGAUSS, DbTypeEnum.find("openGauss"));
        // io.mogdb 的 openGauss JDBC 驱动连接 MogDB 时，返回的 ProductName 与 PostgreSQL 相同
        assertEquals(DbType.POSTGRE_SQL, DbTypeEnum.find("PostgreSQL"));
        assertEquals(DbType.KINGBASE_ES, DbTypeEnum.find("KingbaseES"));
        // 神通 JDBC 驱动（jdbc:oscar:）返回的 ProductName
        assertEquals(DbType.OSCAR, DbTypeEnum.find("OSCAR"));
        assertNull(DbTypeEnum.find(""));
        assertNull(DbTypeEnum.find(null));
    }

    @Test
    public void testFind_unregistered() {
        // 未登记的产品名返回 null，而不是 NPE，由调用方给出明确的处理
        assertNull(DbTypeEnum.find("HighGo"));
        assertNull(DbTypeEnum.find("MariaDB"));
        assertNull(DbTypeEnum.find("SQLite"));
    }

    @Test
    public void testGetFindInSetTemplate_postgreSQLCompatible() {
        // GaussDB、openGauss（PG 兼容模式）与 PostgreSQL 使用相同的 POSITION 模板
        assertEquals(DbTypeEnum.getFindInSetTemplate(DbType.POSTGRE_SQL), DbTypeEnum.getFindInSetTemplate(DbType.GAUSS_DB));
        assertEquals(DbTypeEnum.getFindInSetTemplate(DbType.POSTGRE_SQL), DbTypeEnum.getFindInSetTemplate(DbType.OPENGAUSS));
    }

    @Test
    public void testGetFindInSetTemplate_oscar() {
        // 神通的 CAST(... AS VARCHAR) 不指定长度时为 VARCHAR(1)，所以不使用 CAST
        assertEquals("POSITION(',' || #{value} || ',' IN ',' || #{column} || ',') > 0",
                DbTypeEnum.getFindInSetTemplate(DbType.OSCAR));
    }

}
