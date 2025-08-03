package cn.iocoder.yudao.module.bpm.api.task;

import javax.validation.constraints.NotEmpty;

/**
 * 流程任务 Api 接口
 *
 * @author jason
 */
public interface BpmProcessTaskApi {

    /**
     * 触发流程任务的执行
     *
     * @param processInstanceId 流程实例编号
     * @param taskDefineKey 任务 Key
     */
    void triggerTask(@NotEmpty(message = "流程实例的编号不能为空") String processInstanceId,
                     @NotEmpty(message = "任务 Key 不能为空") String taskDefineKey);

}
