package cn.iocoder.yudao.module.mes.service.md.autocode.strategy.impl;

import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MesMdAutoCodeDatePartStrategy} 的单元测试
 *
 * @author 芋道源码
 */
public class MesMdAutoCodeDatePartStrategyTest {

    private final MesMdAutoCodeDatePartStrategy strategy = new MesMdAutoCodeDatePartStrategy();

    @Test
    public void testGenerate_withDefaultFormat() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setDateFormat(null);
        MesMdAutoCodeContext context = new MesMdAutoCodeContext();

        // 调用
        String result = strategy.generate(part, context);

        // 断言：默认格式为 yyyyMMdd，长度为 8
        assertEquals(8, result.length());
        assertTrue(result.matches("\\d{8}"));
    }

    @Test
    public void testGenerate_withCustomFormat() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setDateFormat("yyyyMM");
        MesMdAutoCodeContext context = new MesMdAutoCodeContext();

        // 调用
        String result = strategy.generate(part, context);
        // 断言：格式为 yyyyMM，长度为 6
        assertEquals(6, result.length());
        assertTrue(result.matches("\\d{6}"));
    }

    @Test
    public void testGenerate_withTimeFormat() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setDateFormat("HHmmss");
        MesMdAutoCodeContext context = new MesMdAutoCodeContext();

        // 调用
        String result = strategy.generate(part, context);
        // 断言：格式为 HHmmss，长度为 6
        assertEquals(6, result.length());
        assertTrue(result.matches("\\d{6}"));
    }

}
