package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotRuleSceneMapper;
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
 * {@link IotRuleSceneServiceImpl} 的简化单元测试类
 * 使用 Mockito 进行纯单元测试，不依赖 Spring 容器
 *
 * @author 芋道源码
 */
public class IotRuleSceneServiceSimpleTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotRuleSceneServiceImpl ruleSceneService;

    @Mock
    private IotRuleSceneMapper ruleSceneMapper;

    @Mock
    private List<IotSceneRuleAction> ruleSceneActions;

    @Mock
    private IotSchedulerManager schedulerManager;

    @Mock
    private IotProductService productService;

    @Mock
    private IotDeviceService deviceService;

    @Test
    public void testCreateRuleScene_success() {
        // 准备参数
        IotRuleSceneSaveReqVO createReqVO = randomPojo(IotRuleSceneSaveReqVO.class, o -> {
            o.setId(null);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setTriggers(Collections.singletonList(randomPojo(IotSceneRuleDO.Trigger.class)));
            o.setActions(Collections.singletonList(randomPojo(IotSceneRuleDO.Action.class)));
        });

        // Mock 行为
        Long expectedId = randomLongId();
        when(ruleSceneMapper.insert(any(IotSceneRuleDO.class))).thenAnswer(invocation -> {
            IotSceneRuleDO ruleScene = invocation.getArgument(0);
            ruleScene.setId(expectedId);
            return 1;
        });

        // 调用
        Long ruleSceneId = ruleSceneService.createRuleScene(createReqVO);

        // 断言
        assertEquals(expectedId, ruleSceneId);
        verify(ruleSceneMapper, times(1)).insert(any(IotSceneRuleDO.class));
    }

    @Test
    public void testUpdateRuleScene_success() {
        // 准备参数
        Long id = randomLongId();
        IotRuleSceneSaveReqVO updateReqVO = randomPojo(IotRuleSceneSaveReqVO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setTriggers(Collections.singletonList(randomPojo(IotSceneRuleDO.Trigger.class)));
            o.setActions(Collections.singletonList(randomPojo(IotSceneRuleDO.Action.class)));
        });

        // Mock 行为
        IotSceneRuleDO existingRuleScene = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));
        when(ruleSceneMapper.selectById(id)).thenReturn(existingRuleScene);
        when(ruleSceneMapper.updateById(any(IotSceneRuleDO.class))).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> ruleSceneService.updateRuleScene(updateReqVO));

        // 验证
        verify(ruleSceneMapper, times(1)).selectById(id);
        verify(ruleSceneMapper, times(1)).updateById(any(IotSceneRuleDO.class));
    }

    @Test
    public void testDeleteRuleScene_success() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO existingRuleScene = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));
        when(ruleSceneMapper.selectById(id)).thenReturn(existingRuleScene);
        when(ruleSceneMapper.deleteById(id)).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> ruleSceneService.deleteRuleScene(id));

        // 验证
        verify(ruleSceneMapper, times(1)).selectById(id);
        verify(ruleSceneMapper, times(1)).deleteById(id);
    }

    @Test
    public void testGetRuleScene() {
        // 准备参数
        Long id = randomLongId();
        IotSceneRuleDO expectedRuleScene = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));

        // Mock 行为
        when(ruleSceneMapper.selectById(id)).thenReturn(expectedRuleScene);

        // 调用
        IotSceneRuleDO result = ruleSceneService.getRuleScene(id);

        // 断言
        assertEquals(expectedRuleScene, result);
        verify(ruleSceneMapper, times(1)).selectById(id);
    }

    @Test
    public void testUpdateRuleSceneStatus_success() {
        // 准备参数
        Long id = randomLongId();
        Integer status = CommonStatusEnum.DISABLE.getStatus();

        // Mock 行为
        IotSceneRuleDO existingRuleScene = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(ruleSceneMapper.selectById(id)).thenReturn(existingRuleScene);
        when(ruleSceneMapper.updateById(any(IotSceneRuleDO.class))).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> ruleSceneService.updateRuleSceneStatus(id, status));

        // 验证
        verify(ruleSceneMapper, times(1)).selectById(id);
        verify(ruleSceneMapper, times(1)).updateById(any(IotSceneRuleDO.class));
    }

    @Test
    public void testExecuteRuleSceneByTimer_success() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO ruleScene = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(ruleSceneMapper.selectById(id)).thenReturn(ruleScene);

        // 调用
        assertDoesNotThrow(() -> ruleSceneService.executeRuleSceneByTimer(id));

        // 验证
        verify(ruleSceneMapper, times(1)).selectById(id);
    }

    @Test
    public void testExecuteRuleSceneByTimer_notExists() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        when(ruleSceneMapper.selectById(id)).thenReturn(null);

        // 调用 - 不存在的场景规则应该不会抛异常，只是记录日志
        assertDoesNotThrow(() -> ruleSceneService.executeRuleSceneByTimer(id));

        // 验证
        verify(ruleSceneMapper, times(1)).selectById(id);
    }

    @Test
    public void testExecuteRuleSceneByTimer_disabled() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO ruleScene = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        });
        when(ruleSceneMapper.selectById(id)).thenReturn(ruleScene);

        // 调用 - 禁用的场景规则应该不会执行，只是记录日志
        assertDoesNotThrow(() -> ruleSceneService.executeRuleSceneByTimer(id));

        // 验证
        verify(ruleSceneMapper, times(1)).selectById(id);
    }
}
