package cn.iocoder.yudao.framework.common.util.collection;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrayUtilsTest {

    @Test
    public void testAppend_null() {
        // 准备参数
        Consumer<Object> object = null;
        Consumer<Object> newElement = o -> {};

        // 调用
        Consumer<Object>[] result = ArrayUtils.append(object, newElement);
        // 断言
        assertEquals(1, result.length);
        assertEquals(newElement, result[0]);
    }

    @Test
    public void testAppend_otNull() {
        // 准备参数
        Consumer<Object> object = o -> {};
        Consumer<Object> newElement = o -> {};

        // 调用
        Consumer<Object>[] result = ArrayUtils.append(object, newElement);
        // 断言
        assertEquals(2, result.length);
        assertEquals(object, result[0]);
        assertEquals(newElement, result[1]);
    }

}
