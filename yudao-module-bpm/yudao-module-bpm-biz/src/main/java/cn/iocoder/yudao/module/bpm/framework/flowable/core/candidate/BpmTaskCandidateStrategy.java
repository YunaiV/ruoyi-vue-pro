package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.Collections;
import java.util.Set;

/**
 * BPM 任务的候选人的策略接口
 *
 * 例如说：分配审批人
 *
 * @author 芋道源码
 */
public interface BpmTaskCandidateStrategy {

    /**
     * 对应策略
     *
     * @return 策略
     */
    BpmTaskCandidateStrategyEnum getStrategy();

    /**
     * 校验参数
     *
     * @param param 参数
     */
    void validateParam(String param);

    /**
     * 基于执行任务，获得任务的候选用户们
     *
     * @param execution 执行任务
     * @return 用户编号集合
     */
    Set<Long> calculateUsers(DelegateExecution execution, String param);


    /**
     * 基于流程实例，获得任务的候选用户们。 用于获取未执行节点的候选用户们
     *
     * @param processInstanceId 流程实例
     * @param param 节点的参数
     * @return 用户编号集合
     */
    default Set<Long> calculateUsers(String processInstanceId, String param) {
        return Collections.emptySet();
    }

    /**
     * 是否一定要输入参数
     *
     * @return 是否
     */
    default boolean isParamRequired() {
        return true;
    }

}
