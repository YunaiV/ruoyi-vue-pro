package cn.iocoder.yudao.module.iot.service.rule.scene.timer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.framework.job.core.IotSchedulerManager;
import cn.iocoder.yudao.module.iot.job.rule.IotSceneRuleJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

/**
 * IoT 场景规则定时触发器处理器：负责管理定时触发器的注册、更新、删除等操作
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotSceneRuleTimerHandler {

    @Resource(name = "iotSchedulerManager")
    private IotSchedulerManager schedulerManager;

    /**
     * 注册场景规则的定时触发器
     *
     * @param sceneRule 场景规则
     */
    public void registerTimerTriggers(IotSceneRuleDO sceneRule) {
        // 1. 过滤出定时触发器
        if (sceneRule == null || CollUtil.isEmpty(sceneRule.getTriggers())) {
            return;
        }
        List<IotSceneRuleDO.Trigger> timerTriggers = filterList(sceneRule.getTriggers(),
                trigger -> ObjUtil.equals(trigger.getType(), IotSceneRuleTriggerTypeEnum.TIMER.getType()));
        if (CollUtil.isEmpty(timerTriggers)) {
            return;
        }

        // 2. 注册每个定时触发器
        timerTriggers.forEach(trigger -> registerSingleTimerTrigger(sceneRule, trigger));
    }

    /**
     * 更新场景规则的定时触发器
     *
     * @param sceneRule 场景规则
     */
    public void updateTimerTriggers(IotSceneRuleDO sceneRule) {
        if (sceneRule == null) {
            return;
        }

        // 1. 先删除旧的定时任务
        unregisterTimerTriggers(sceneRule.getId());

        // 2.1 如果场景规则已禁用，则不重新注册
        if (CommonStatusEnum.isDisable(sceneRule.getStatus())) {
            log.info("[updateTimerTriggers][场景规则({}) 已禁用，不注册定时触发器]", sceneRule.getId());
            return;
        }

        // 2.2 重新注册定时触发器
        registerTimerTriggers(sceneRule);
    }

    /**
     * 注销场景规则的定时触发器
     *
     * @param sceneRuleId 场景规则 ID
     */
    public void unregisterTimerTriggers(Long sceneRuleId) {
        if (sceneRuleId == null) {
            return;
        }

        String jobName = buildJobName(sceneRuleId);
        try {
            schedulerManager.deleteJob(jobName);
            log.info("[unregisterTimerTriggers][场景规则({}) 定时触发器注销成功]", sceneRuleId);
        } catch (SchedulerException e) {
            log.error("[unregisterTimerTriggers][场景规则({}) 定时触发器注销失败]", sceneRuleId, e);
        }
    }

    /**
     * 暂停场景规则的定时触发器
     *
     * @param sceneRuleId 场景规则 ID
     */
    public void pauseTimerTriggers(Long sceneRuleId) {
        if (sceneRuleId == null) {
            return;
        }

        String jobName = buildJobName(sceneRuleId);
        try {
            schedulerManager.pauseJob(jobName);
            log.info("[pauseTimerTriggers][场景规则({}) 定时触发器暂停成功]", sceneRuleId);
        } catch (SchedulerException e) {
            log.error("[pauseTimerTriggers][场景规则({}) 定时触发器暂停失败]", sceneRuleId, e);
        }
    }

    /**
     * 注册单个定时触发器
     *
     * @param sceneRule 场景规则
     * @param trigger   定时触发器配置
     */
    private void registerSingleTimerTrigger(IotSceneRuleDO sceneRule, IotSceneRuleDO.Trigger trigger) {
        // 1. 参数校验
        if (StrUtil.isBlank(trigger.getCronExpression())) {
            log.error("[registerSingleTimerTrigger][场景规则({}) 定时触发器缺少 CRON 表达式]", sceneRule.getId());
            return;
        }

        try {
            // 2.1 构建任务名称和数据
            String jobName = buildJobName(sceneRule.getId());
            // 2.2 注册定时任务
            schedulerManager.addOrUpdateJob(
                    IotSceneRuleJob.class,
                    jobName,
                    trigger.getCronExpression(),
                    IotSceneRuleJob.buildJobDataMap(sceneRule.getId())
            );
            log.info("[registerSingleTimerTrigger][场景规则({}) 定时触发器注册成功，CRON: {}]",
                    sceneRule.getId(), trigger.getCronExpression());
        } catch (SchedulerException e) {
            log.error("[registerSingleTimerTrigger][场景规则({}) 定时触发器注册失败，CRON: {}]",
                    sceneRule.getId(), trigger.getCronExpression(), e);
        }
    }

    /**
     * 构建任务名称
     *
     * @param sceneRuleId 场景规则 ID
     * @return 任务名称
     */
    private String buildJobName(Long sceneRuleId) {
        return "iot_scene_rule_timer_" + sceneRuleId;
    }

}
