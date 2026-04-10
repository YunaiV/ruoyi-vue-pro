package cn.iocoder.yudao.module.mes.service.md.autocode.strategy.impl;

import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MesMdAutoCodeInputCharPartStrategy} 的单元测试
 *
 * @author 芋道源码
 */
public class MesMdAutoCodeInputCharPartStrategyTest {

    private final MesMdAutoCodeInputCharPartStrategy strategy = new MesMdAutoCodeInputCharPartStrategy();

    @Test
    public void testGenerate_withInputChar() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO();
        MesMdAutoCodeContext context = new MesMdAutoCodeContext().setInputChar("ABC");

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("ABC", result);
    }

    @Test
    public void testGenerate_withEmptyInputChar() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO();
        MesMdAutoCodeContext context = new MesMdAutoCodeContext().setInputChar("");

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("", result);
    }

    @Test
    public void testGenerate_withNullInputChar() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO();
        MesMdAutoCodeContext context = new MesMdAutoCodeContext().setInputChar(null);

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("", result);
    }

}
