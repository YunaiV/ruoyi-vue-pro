package cn.iocoder.yudao.module.mes.service.md.autocode.strategy.impl;

import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MesMdAutoCodeFixedCharPartStrategy} 的单元测试
 *
 * @author 芋道源码
 */
public class MesMdAutoCodeFixedCharPartStrategyTest {

    private final MesMdAutoCodeFixedCharPartStrategy strategy = new MesMdAutoCodeFixedCharPartStrategy();

    @Test
    public void testGenerate_withFixedChar() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setFixCharacter("ITEM_");
        MesMdAutoCodeContext context = new MesMdAutoCodeContext();

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("ITEM_", result);
    }

    @Test
    public void testGenerate_withEmptyFixedChar() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setFixCharacter("");
        MesMdAutoCodeContext context = new MesMdAutoCodeContext();

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("", result);
    }

    @Test
    public void testGenerate_withNullFixedChar() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setFixCharacter(null);
        MesMdAutoCodeContext context = new MesMdAutoCodeContext();

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("", result);
    }

}
