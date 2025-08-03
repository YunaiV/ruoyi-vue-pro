package cn.iocoder.yudao.module.iot.job.rule;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.IotRuleSceneService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Map;

/**
 * IoT 规则场景 Job，用于执行 {@link IotRuleSceneTriggerTypeEnum#TIMER} 类型的规则场景
 *
 * @author 芋道源码
 */
@Slf4j
public class IotRuleSceneJob extends QuartzJobBean {

    /**
     * JobData Key - 规则场景编号
     */
    public static final String JOB_DATA_KEY_RULE_SCENE_ID = "ruleSceneId";

    @Resource
    private IotRuleSceneService ruleSceneService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        // 获得规则场景编号
        Long ruleSceneId = context.getMergedJobDataMap().getLong(JOB_DATA_KEY_RULE_SCENE_ID);

        // 执行规则场景
        ruleSceneService.executeRuleSceneByTimer(ruleSceneId);
    }

    /**
     * 创建 JobData Map
     *
     * @param ruleSceneId 规则场景编号
     * @return JobData Map
     */
    public static Map<String, Object> buildJobDataMap(Long ruleSceneId) {
        return MapUtil.of(JOB_DATA_KEY_RULE_SCENE_ID, ruleSceneId);
    }

    /**
     * 创建 Job 名字
     *
     * @param ruleSceneId 规则场景编号
     * @return Job 名字
     */
    public static String buildJobName(Long ruleSceneId) {
        return String.format("%s_%d", IotRuleSceneJob.class.getSimpleName(), ruleSceneId);
    }

}
