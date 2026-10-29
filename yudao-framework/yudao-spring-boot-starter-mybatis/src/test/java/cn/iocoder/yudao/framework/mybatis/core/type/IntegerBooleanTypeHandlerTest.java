package cn.iocoder.yudao.framework.mybatis.core.type;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link IntegerBooleanTypeHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IntegerBooleanTypeHandlerTest {

    private final IntegerBooleanTypeHandler handler = new IntegerBooleanTypeHandler();

    // ========== 写入 ==========

    @Test
    public void testSetParameter_true() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setParameter(ps, 1, true, null);

        verify(ps).setInt(1, 1);
        verify(ps, never()).setBoolean(anyInt(), anyBoolean());
    }

    @Test
    public void testSetParameter_false() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setParameter(ps, 2, false, JdbcType.TINYINT);

        verify(ps).setInt(2, 0);
    }

    @Test
    public void testSetParameter_nullWithoutJdbcType() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);

        // MyBatis 未指定 jdbcType 时，可能传入 null 或 jdbcTypeForNull 默认的 OTHER
        handler.setParameter(ps, 1, null, null);
        handler.setParameter(ps, 2, null, JdbcType.OTHER);

        verify(ps).setNull(1, Types.INTEGER);
        verify(ps).setNull(2, Types.INTEGER);
    }

    @Test
    public void testSetParameter_nullWithJdbcType() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setParameter(ps, 1, null, JdbcType.SMALLINT);

        verify(ps).setNull(1, Types.SMALLINT);
    }

    // ========== 读取 ==========

    @Test
    public void testGetResult_byColumnName() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("used")).thenReturn(1, 0, 2, 0);
        when(rs.wasNull()).thenReturn(false, false, false, true);

        assertEquals(Boolean.TRUE, handler.getResult(rs, "used"));
        assertEquals(Boolean.FALSE, handler.getResult(rs, "used"));
        assertEquals(Boolean.TRUE, handler.getResult(rs, "used")); // 非 0 均为 true
        assertNull(handler.getResult(rs, "used")); // NULL
    }

    @Test
    public void testGetResult_byColumnIndex() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt(3)).thenReturn(1, 0, 0);
        when(rs.wasNull()).thenReturn(false, false, true);

        assertEquals(Boolean.TRUE, handler.getResult(rs, 3));
        assertEquals(Boolean.FALSE, handler.getResult(rs, 3));
        assertNull(handler.getResult(rs, 3));
    }

    @Test
    public void testGetResult_callableStatement() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getInt(1)).thenReturn(1, 0, 0);
        when(cs.wasNull()).thenReturn(false, false, true);

        assertEquals(Boolean.TRUE, handler.getResult(cs, 1));
        assertEquals(Boolean.FALSE, handler.getResult(cs, 1));
        assertNull(handler.getResult(cs, 1));
    }

}
