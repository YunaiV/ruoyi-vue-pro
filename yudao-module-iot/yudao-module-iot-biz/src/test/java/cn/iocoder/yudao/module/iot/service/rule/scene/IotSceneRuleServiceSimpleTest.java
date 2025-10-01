package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotSceneRuleMapper;
import cn.iocoder.yudao.module.iot.framework.job.core.IotSchedulerManager;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.scene.action.IotSceneRuleAction;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotSceneRuleServiceImpl} 的简化单元测试类
 * 使用 Mockito 进行纯单元测试，不依赖 Spring 容器
 *
 * @author 芋道源码
 */
public class IotSceneRuleServiceSimpleTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotSceneRuleServiceImpl sceneRuleService;

    @Mock
    private IotSceneRuleMapper sceneRuleMapper;

    @Mock
    private List<IotSceneRuleAction> sceneRuleActions;

    @Mock
    private IotSchedulerManager schedulerManager;

    @Mock
    private IotProductService productService;

    @Mock
    private IotDeviceService deviceService;

    @Test
    public void testCreateScene_Rule_success() {
        // 准备参数
        IotSceneRuleSaveReqVO createReqVO = randomPojo(IotSceneRuleSaveReqVO.class, o -> {
            o.setId(null);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setTriggers(Collections.singletonList(randomPojo(IotSceneRuleDO.Trigger.class)));
            o.setActions(Collections.singletonList(randomPojo(IotSceneRuleDO.Action.class)));
        });

        // Mock 行为
        Long expectedId = randomLongId();
        when(sceneRuleMapper.insert(any(IotSceneRuleDO.class))).thenAnswer(invocation -> {
            IotSceneRuleDO sceneRule = invocation.getArgument(0);
            sceneRule.setId(expectedId);
            return 1;
        });

        // 调用
        Long sceneRuleId = sceneRuleService.createSceneRule(createReqVO);

        // 断言
        assertEquals(expectedId, sceneRuleId);
        verify(sceneRuleMapper, times(1)).insert(any(IotSceneRuleDO.class));
    }

    @Test
    public void testUpdateScene_Rule_success() {
        // 准备参数
        Long id = randomLongId();
        IotSceneRuleSaveReqVO updateReqVO = randomPojo(IotSceneRuleSaveReqVO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setTriggers(Collections.singletonList(randomPojo(IotSceneRuleDO.Trigger.class)));
            o.setActions(Collections.singletonList(randomPojo(IotSceneRuleDO.Action.class)));
        });

        // Mock 行为
        IotSceneRuleDO existingSceneRule = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));
        when(sceneRuleMapper.selectById(id)).thenReturn(existingSceneRule);
        when(sceneRuleMapper.updateById(any(IotSceneRuleDO.class))).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.updateSceneRule(updateReqVO));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
        verify(sceneRuleMapper, times(1)).updateById(any(IotSceneRuleDO.class));
    }

    @Test
    public void testDeleteSceneRule_success() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO existingSceneRule = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));
        when(sceneRuleMapper.selectById(id)).thenReturn(existingSceneRule);
        when(sceneRuleMapper.deleteById(id)).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.deleteSceneRule(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
        verify(sceneRuleMapper, times(1)).deleteById(id);
    }

    @Test
    public void testGetSceneRule() {
        // 准备参数
        Long id = randomLongId();
        IotSceneRuleDO expectedSceneRule = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));

        // Mock 行为
        when(sceneRuleMapper.selectById(id)).thenReturn(expectedSceneRule);

        // 调用
        IotSceneRuleDO result = sceneRuleService.getSceneRule(id);

        // 断言
        assertEquals(expectedSceneRule, result);
        verify(sceneRuleMapper, times(1)).selectById(id);
    }

    @Test
    public void testUpdateSceneRuleStatus_success() {
        // 准备参数
        Long id = randomLongId();
        Integer status = CommonStatusEnum.DISABLE.getStatus();

        // Mock 行为
        IotSceneRuleDO existingSceneRule = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(sceneRuleMapper.selectById(id)).thenReturn(existingSceneRule);
        when(sceneRuleMapper.updateById(any(IotSceneRuleDO.class))).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.updateSceneRuleStatus(id, status));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
        verify(sceneRuleMapper, times(1)).updateById(any(IotSceneRuleDO.class));
    }

    @Test
    public void testExecuteSceneRuleByTimer_success() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO sceneRule = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(sceneRuleMapper.selectById(id)).thenReturn(sceneRule);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
    }

    @Test
    public void testExecuteSceneRuleByTimer_notExists() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        when(sceneRuleMapper.selectById(id)).thenReturn(null);

        // 调用 - 不存在的场景规则应该不会抛异常，只是记录日志
        assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
    }

    @Test
    public void testExecuteSceneRuleByTimer_disabled() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO sceneRule = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        });
        when(sceneRuleMapper.selectById(id)).thenReturn(sceneRule);

        // 调用 - 禁用的场景规则应该不会执行，只是记录日志
        assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
    }
}
