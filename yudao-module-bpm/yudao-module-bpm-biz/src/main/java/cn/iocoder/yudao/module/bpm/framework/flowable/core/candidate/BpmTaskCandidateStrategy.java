package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.Collections;
import java.util.Set;

/**
 * BPM 任务的候选人的策略接口
 * <p>
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
     * 是否一定要输入参数
     *
     * @return 是否
     */
    default boolean isParamRequired() {
        return true;
    }

    /**
     * 基于候选人参数，获得任务的候选用户们
     *
     * @param param 执行任务
     * @return 用户编号集合
     */
    default Set<Long> calculateUsers(String param) {
        return Collections.emptySet();
    }

    /**
     * 基于执行任务，获得任务的候选用户们
     *
     * @param execution 执行任务
     * @return 用户编号集合
     */
    default Set<Long> calculateUsers(DelegateExecution execution, String param) {
        return calculateUsers(param);
    }


    /**
     * 基于流程实例，获得任务的候选用户们
     * <p>
     * 目的：用于获取未执行节点的候选用户们
     *
     * @param startUserId  流程发起人编号
     * @param processInstance 流程实例编号
     * @param activityId 活动 Id (对应 Bpmn XML id)
     * @param param     节点的参数
     * @return 用户编号集合
     */
    default Set<Long> calculateUsers(Long startUserId, ProcessInstance processInstance, String activityId, String param) {
        return calculateUsers(param);
    }

    // TODO @芋艿：后续可以抽象一个 calculateUsers(String param)，默认 calculateUsers 和 calculateUsers 调用它
    // TODO @芋艿 加了， review 一下

}
