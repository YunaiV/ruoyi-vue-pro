package cn.iocoder.yudao.module.mes.service.md.autocode.strategy.impl;

import cn.iocoder.yudao.framework.test.core.ut.BaseRedisUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import cn.iocoder.yudao.module.mes.dal.redis.md.autocode.MesMdAutoCodeRedisDAO;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodeCycleMethodEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MesMdAutoCodeSerialNumberPartStrategy} 的单元测试
 *
 * @author 芋道源码
 */
@Import({MesMdAutoCodeSerialNumberPartStrategy.class, MesMdAutoCodeRedisDAO.class})
public class MesMdAutoCodeSerialNumberPartStrategyTest extends BaseRedisUnitTest {

    @Resource
    private MesMdAutoCodeSerialNumberPartStrategy strategy;

    @Test
    public void testGenerate_noCycle() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setLength(4).setSerialStartNo(1).setSerialStep(1).setCycleFlag(false);
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(1L);
        MesMdAutoCodeContext context = new MesMdAutoCodeContext().setRule(rule);

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("0001", result);
        assertEquals(1, context.getSerialNo());
    }

    @Test
    public void testGenerate_withCycleByDay() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setLength(4).setSerialStartNo(1).setSerialStep(1)
                .setCycleFlag(true).setCycleMethod(MesMdAutoCodeCycleMethodEnum.DAY.getMethod());
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(2L);
        MesMdAutoCodeContext context = new MesMdAutoCodeContext().setRule(rule);

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("0001", result);
        assertEquals(1, context.getSerialNo());
    }

    @Test
    public void testGenerate_withCycleByInputChar() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setLength(4).setSerialStartNo(1).setSerialStep(1)
                .setCycleFlag(true).setCycleMethod(MesMdAutoCodeCycleMethodEnum.INPUT_CHAR.getMethod());
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(3L);
        MesMdAutoCodeContext context = new MesMdAutoCodeContext().setRule(rule).setInputChar("A");

        // 调用
        String result = strategy.generate(part, context);
        // 断言
        assertEquals("0001", result);
        assertEquals(1, context.getSerialNo());
    }

    @Test
    public void testGenerate_withStep() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setLength(4).setSerialStartNo(1).setSerialStep(5).setCycleFlag(false);
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(4L);
        MesMdAutoCodeContext context1 = new MesMdAutoCodeContext().setRule(rule);
        MesMdAutoCodeContext context2 = new MesMdAutoCodeContext().setRule(rule);

        // 调用
        String result1 = strategy.generate(part, context1);
        String result2 = strategy.generate(part, context2);
        // 断言
        assertEquals("0001", result1);
        assertEquals(1, context1.getSerialNo());
        assertEquals("0006", result2);
        assertEquals(6, context2.getSerialNo());
    }

    @Test
    public void testGenerate_multipleCallsIncrement() {
        // 准备参数
        MesMdAutoCodePartDO part = new MesMdAutoCodePartDO().setLength(4).setSerialStartNo(1).setSerialStep(1).setCycleFlag(false);
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(5L);
        MesMdAutoCodeContext context1 = new MesMdAutoCodeContext().setRule(rule);
        MesMdAutoCodeContext context2 = new MesMdAutoCodeContext().setRule(rule);

        // 调用
        String result1 = strategy.generate(part, context1);
        String result2 = strategy.generate(part, context2);
        // 断言
        assertEquals("0001", result1);
        assertEquals("0002", result2);
    }

}
