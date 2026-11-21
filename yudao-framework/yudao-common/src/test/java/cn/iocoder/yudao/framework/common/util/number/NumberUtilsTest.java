package cn.iocoder.yudao.framework.common.util.number;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NumberUtilsTest {

    @Test
    void testDiv() {
        assertEquals(2D, NumberUtils.div(6L, 3L));
        assertEquals(0.33D, NumberUtils.div(1L, 3L));
        assertEquals(0.67D, NumberUtils.div(2L, 3L));
    }

    @Test
    void testDiv_nullOrZero() {
        assertEquals(0D, NumberUtils.div(null, 3L));
        assertEquals(0D, NumberUtils.div(3L, null));
        assertEquals(0D, NumberUtils.div(null, null));
        assertEquals(0D, NumberUtils.div(3L, 0L));
        assertEquals(0D, NumberUtils.div(0L, 3L));
    }

    @Test
    void testDiv_halfUp() {
        assertEquals(0.13D, NumberUtils.div(1L, 8L));
        assertEquals(-0.13D, NumberUtils.div(-1L, 8L));
        assertEquals(-0.13D, NumberUtils.div(1L, -8L));
    }

    @Test
    void testDiv_largeValues() {
        assertEquals(1D, NumberUtils.div(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(1D, NumberUtils.div(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    @Test
    void testGetPlainStringLength() {
        // 普通数字：与去掉末尾 0 后 toPlainString 的长度一致
        for (String value : List.of("123", "-123", "0", "-0", "0.000", "12.3400", "-0.05", "123.45", "100", "1.5E+3",
                "-9223372036854775808", "0.00000000001")) {
            BigDecimal number = new BigDecimal(value);
            assertEquals(number.stripTrailingZeros().toPlainString().length(), NumberUtils.getPlainStringLength(number), value);
        }
        // 零与负零均为 0；末尾 0 不计入长度
        assertEquals(1L, NumberUtils.getPlainStringLength(new BigDecimal("-0.00")));
        assertEquals(5L, NumberUtils.getPlainStringLength(new BigDecimal("12.3400")));
        assertEquals(4L, NumberUtils.getPlainStringLength(new BigDecimal("1.5E+3")));
    }

    @Test
    void testGetPlainStringLength_boundary() {
        // 100、101 个字符的边界：整数 1 后接 99、100 个 0；小数 0. 后接 97、98 个 0 再接 1
        assertEquals(100L, NumberUtils.getPlainStringLength(new BigDecimal("1E+99")));
        assertEquals(101L, NumberUtils.getPlainStringLength(new BigDecimal("1E+100")));
        assertEquals(100L, NumberUtils.getPlainStringLength(new BigDecimal("1E-98")));
        assertEquals(101L, NumberUtils.getPlainStringLength(new BigDecimal("1E-99")));
        assertEquals(101L, NumberUtils.getPlainStringLength(new BigDecimal("-1E+99")));
        assertEquals(101, new BigDecimal("1E+100").toPlainString().length());
    }

    @Test
    void testGetPlainStringLength_hugeExponent() {
        // scale 为 int 的上下界：按 long 计算，不溢出，也不展开文本
        assertEquals(2147483648L, NumberUtils.getPlainStringLength(new BigDecimal("1E+2147483647")));
        assertEquals(2147483649L, NumberUtils.getPlainStringLength(new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE)));
        assertEquals(2147483649L, NumberUtils.getPlainStringLength(new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE)));
        assertEquals(2147483650L, NumberUtils.getPlainStringLength(new BigDecimal(BigInteger.ONE.negate(), Integer.MAX_VALUE)));
    }

    @Test
    void testGetIntegerBound() {
        long min = Integer.MIN_VALUE;
        long max = Integer.MAX_VALUE;
        // 等于极值：原样返回
        assertEquals(max, NumberUtils.getIntegerBound(BigDecimal.valueOf(max), true, min, max));
        assertEquals(min, NumberUtils.getIntegerBound(BigDecimal.valueOf(min), false, min, max));
        // 越界但仍可钳制：下边界小于最小值、上边界大于最大值
        assertEquals(min, NumberUtils.getIntegerBound(new BigDecimal("-3000000000"), true, min, max));
        assertEquals(max, NumberUtils.getIntegerBound(new BigDecimal("3000000000"), false, min, max));
        // 越界无可匹配值：下边界大于最大值、上边界小于最小值
        assertNull(NumberUtils.getIntegerBound(new BigDecimal("3000000000"), true, min, max));
        assertNull(NumberUtils.getIntegerBound(new BigDecimal("-3000000000"), false, min, max));
    }

}
