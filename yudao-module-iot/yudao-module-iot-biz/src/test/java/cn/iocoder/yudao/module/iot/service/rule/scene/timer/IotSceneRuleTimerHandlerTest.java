package cn.iocoder.yudao.module.iot.service.rule.scene.timer;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.framework.job.core.IotSchedulerManager;
import cn.iocoder.yudao.module.iot.job.rule.IotSceneRuleJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotSceneRuleTimerHandler} 的单元测试类
 *
 * @author HUIHUI
 */
@ExtendWith(MockitoExtension.class)
public class IotSceneRuleTimerHandlerTest {

    @Mock
    private IotSchedulerManager schedulerManager;

    @InjectMocks
    private IotSceneRuleTimerHandler timerHandler;

    @BeforeEach
    void setUp() {
        // 重置 Mock 对象
        reset(schedulerManager);
    }

    @Test
    public void testRegisterTimerTriggers_success() throws SchedulerException {
        // 准备参数
        Long sceneRuleId = 1L;
        IotSceneRuleDO sceneRule = new IotSceneRuleDO();
        sceneRule.setId(sceneRuleId);
        sceneRule.setStatus(0); // 0 表示启用
        // 创建定时触发器
        IotSceneRuleDO.Trigger timerTrigger = new IotSceneRuleDO.Trigger();
        timerTrigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        timerTrigger.setCronExpression("0 0 12 * * ?"); // 每天中午12点
        sceneRule.setTriggers(ListUtil.toList(timerTrigger));

        // 调用
        timerHandler.registerTimerTriggers(sceneRule);

        // 验证
        verify(schedulerManager, times(1)).addOrUpdateJob(
                eq(IotSceneRuleJob.class),
                eq("iot_scene_rule_timer_" + sceneRuleId),
                eq("0 0 12 * * ?"),
                eq(IotSceneRuleJob.buildJobDataMap(sceneRuleId))
        );
    }

    @Test
    public void testRegisterTimerTriggers_noTimerTrigger() throws SchedulerException {
        // 准备参数 - 没有定时触发器
        IotSceneRuleDO sceneRule = new IotSceneRuleDO();
        sceneRule.setStatus(0); // 0 表示启用
        // 创建非定时触发器
        IotSceneRuleDO.Trigger deviceTrigger = new IotSceneRuleDO.Trigger();
        deviceTrigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST.getType());
        sceneRule.setTriggers(ListUtil.toList(deviceTrigger));

        // 调用
        timerHandler.registerTimerTriggers(sceneRule);

        // 验证 - 不应该调用调度器
        verify(schedulerManager, never()).addOrUpdateJob(any(), any(), any(), any());
    }

    @Test
    public void testRegisterTimerTriggers_emptyCronExpression() throws SchedulerException {
        // 准备参数 - CRON 表达式为空
        Long sceneRuleId = 2L;
        IotSceneRuleDO sceneRule = new IotSceneRuleDO();
        sceneRule.setId(sceneRuleId);
        sceneRule.setStatus(0); // 0 表示启用
        // 创建定时触发器但没有 CRON 表达式
        IotSceneRuleDO.Trigger timerTrigger = new IotSceneRuleDO.Trigger();
        timerTrigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        timerTrigger.setCronExpression(""); // 空的 CRON 表达式
        sceneRule.setTriggers(ListUtil.toList(timerTrigger));

        // 调用
        timerHandler.registerTimerTriggers(sceneRule);

        // 验证 - 不应该调用调度器
        verify(schedulerManager, never()).addOrUpdateJob(any(), any(), any(), any());
    }

    @Test
    public void testUnregisterTimerTriggers_success() throws SchedulerException {
        // 准备参数
        Long sceneRuleId = 3L;

        // 调用
        timerHandler.unregisterTimerTriggers(sceneRuleId);

        // 验证
        verify(schedulerManager, times(1)).deleteJob("iot_scene_rule_timer_" + sceneRuleId);
    }

    @Test
    public void testPauseTimerTriggers_success() throws SchedulerException {
        // 准备参数
        Long sceneRuleId = 4L;

        // 调用
        timerHandler.pauseTimerTriggers(sceneRuleId);

        // 验证
        verify(schedulerManager, times(1)).pauseJob("iot_scene_rule_timer_" + sceneRuleId);
    }

}
