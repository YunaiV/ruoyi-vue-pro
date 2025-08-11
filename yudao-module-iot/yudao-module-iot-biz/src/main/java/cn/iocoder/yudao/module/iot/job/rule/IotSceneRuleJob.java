package cn.iocoder.yudao.module.iot.job.rule;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Map;

/**
 * IoT 规则场景 Job，用于执行 {@link IotSceneRuleTriggerTypeEnum#TIMER} 类型的规则场景
 *
 * @author 芋道源码
 */
@Slf4j
public class IotSceneRuleJob extends QuartzJobBean {

    /**
     * JobData Key - 规则场景编号
     */
    public static final String JOB_DATA_KEY_RULE_SCENE_ID = "sceneRuleId";

    @Resource
    private IotSceneRuleService sceneRuleService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        // 获得规则场景编号
        Long sceneRuleId = context.getMergedJobDataMap().getLong(JOB_DATA_KEY_RULE_SCENE_ID);

        // 执行规则场景
        sceneRuleService.executeSceneRuleByTimer(sceneRuleId);
    }

    /**
     * 创建 JobData Map
     *
     * @param sceneRuleId 规则场景编号
     * @return JobData Map
     */
    public static Map<String, Object> buildJobDataMap(Long sceneRuleId) {
        return MapUtil.of(JOB_DATA_KEY_RULE_SCENE_ID, sceneRuleId);
    }

    /**
     * 创建 Job 名字
     *
     * @param sceneRuleId 规则场景编号
     * @return Job 名字
     */
    public static String buildJobName(Long sceneRuleId) {
        return String.format("%s_%d", IotSceneRuleJob.class.getSimpleName(), sceneRuleId);
    }

}
