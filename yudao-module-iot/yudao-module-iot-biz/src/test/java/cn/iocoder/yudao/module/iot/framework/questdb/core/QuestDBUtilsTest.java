package cn.iocoder.yudao.module.iot.framework.questdb.core;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


/**
 * {@link QuestDBUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class QuestDBUtilsTest {

    @Test
    public void testIsTableNotExists_wrapped() {
        // 准备参数：QuestDB 通过 PGWire 返回的真实消息，被 Spring 包装
        SQLException cause = new SQLException("ERROR: table does not exist [table=product_property_1]\n  Position: 15", "00000");
        BadSqlGrammarException exception = new BadSqlGrammarException("selectHistoryList", "SELECT ...", cause);

        // 调用，并断言
        assertTrue(QuestDBUtils.isTableNotExists(exception));
    }

    @Test
    public void testIsTableNotExists_otherErrors() {
        // 列不存在、非 SQLException、空异常，都不是表不存在
        assertFalse(QuestDBUtils.isTableNotExists(new SQLException("ERROR: Invalid column: p_zz", "00000")));
        assertFalse(QuestDBUtils.isTableNotExists(new IllegalStateException("table does not exist")));
        assertFalse(QuestDBUtils.isTableNotExists(null));
    }

    @Test
    public void testToMicros() {
        assertEquals(1_700_000_000_000_000L, QuestDBUtils.toMicros(1_700_000_000_000L));
        assertNull(QuestDBUtils.toMicros(null));
        assertThrows(ArithmeticException.class, () -> QuestDBUtils.toMicros(Long.MAX_VALUE));
    }

}
