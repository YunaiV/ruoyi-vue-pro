package cn.iocoder.yudao.framework.common.util.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StrUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class StrUtilsTest {

    @Test
    public void testMaskName() {
        // 调用，并断言：保留首字，其余每个字替换为 *，单字姓名为首字加 *
        assertEquals("张*", StrUtils.maskName("张三"));
        assertEquals("张*", StrUtils.maskName("张"));
        assertEquals("欧***", StrUtils.maskName("欧阳娜娜"));
        // 调用，并断言：去除首尾空白
        assertEquals("李*", StrUtils.maskName("  李四 "));
        // 调用，并断言：按码点截取，不拆开代理对
        assertEquals("𠮷*", StrUtils.maskName("𠮷野"));
        // 调用，并断言：为空时返回空字符串
        assertEquals("", StrUtils.maskName(null));
        assertEquals("", StrUtils.maskName(" "));
    }

}
