package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerTypeEnum;
import jakarta.validation.Valid;
import org.flowable.task.api.Task;



/**
 * 流程任务实例【写】Service 接口
 *
 * @author 芋道源码
 */
public interface BpmTaskUpdateService {

    // ========== Update 写入相关方法 ==========

    /**
     * 通过任务
     *
     * @param userId 用户编号
     * @param reqVO  通过请求
     */
    void approveTask(Long userId, @Valid BpmTaskApproveReqVO reqVO);

    /**
     * 不通过任务
     *
     * @param userId 用户编号
     * @param reqVO  不通过请求
     */
    void rejectTask(Long userId, @Valid BpmTaskRejectReqVO reqVO);

    /**
     * 将流程任务分配给指定用户
     *
     * @param userId 用户编号
     * @param reqVO  分配请求
     */
    void transferTask(Long userId, BpmTaskTransferReqVO reqVO);

    /**
     * 将指定流程实例的、进行中的流程任务，移动到结束节点
     *
     * @param processInstanceId 流程编号
     * @param reason 原因
     */
    void moveTaskToEnd(String processInstanceId, String reason);

    /**
     * 将任务退回到指定的 targetDefinitionKey 位置
     *
     * @param userId 用户编号
     * @param reqVO  退回的任务key和当前所在的任务ID
     */
    void returnTask(Long userId, BpmTaskReturnReqVO reqVO);

    /**
     * 退回修改后重新提交：修改人修改表单后，任务恢复为审批中，由原审批人继续审批
     *
     * @param userId 用户编号，即修改人
     * @param reqVO  重新提交请求
     */
    void resubmitTask(Long userId, @Valid BpmTaskResubmitReqVO reqVO);

    /**
     * 将指定任务委派给其他人处理，等接收人处理后再回到原审批人手中审批
     *
     * @param userId 用户编号
     * @param reqVO  被委派人和被委派的任务编号理由参数
     */
    void delegateTask(Long userId, BpmTaskDelegateReqVO reqVO);

    /**
     * 任务加签
     *
     * @param userId 被加签的用户和任务 ID，加签类型
     * @param reqVO  当前用户 ID
     */
    void createSignTask(Long userId, BpmTaskSignCreateReqVO reqVO);

    /**
     * 任务减签
     *
     * @param userId 当前用户ID
     * @param reqVO  被减签的任务 ID，理由
     */
    void deleteSignTask(Long userId, BpmTaskSignDeleteReqVO reqVO);

    /**
     * 抄送任务
     *
     * @param userId 用户编号
     * @param reqVO  通过请求
     */
    void copyTask(Long userId, @Valid BpmTaskCopyReqVO reqVO);

    /**
     * 撤回任务
     *
     * @param userId 用户编号
     * @param taskId 任务编号
     */
    void withdrawTask(Long userId, String taskId);

    // ========== Event 事件相关方法 ==========

    /**
     * 处理 Task 创建事件，目前是
     * <p>
     * 1. 更新它的状态为审批中
     * 2. 处理自动通过的情况，例如说：1）无审批人时，是否自动通过、不通过；2）非【人工审核】时，是否自动通过、不通过
     * <p>
     * 注意：它的触发时机，晚于 {@link #processTaskAssigned(Task)} 之后
     *
     * @param task 任务实体
     */
    void processTaskCreated(Task task);

    /**
     * 处理 Task 取消事件，目前是更新它的状态为已取消
     *
     * @param taskId 任务的编号
     */
    void processTaskCanceled(String taskId);

    /**
     * 处理 Task 设置审批人事件，目前是发送审批消息
     *
     * @param task 任务实体
     */
    void processTaskAssigned(Task task);

    /**
     * 处理 Task 完成事件，目前是发送任务后置通知
     *
     * @param task 任务实体
     */
    void processTaskCompleted(Task task);

    /**
     * 处理 Task 审批超时事件，可能会处理多个当前审批中的任务
     *
     * @param processInstanceId 流程示例编号
     * @param taskDefineKey     任务 Key
     * @param handlerType       处理类型，参见 {@link BpmUserTaskTimeoutHandlerTypeEnum}
     */
    void processTaskTimeout(String processInstanceId, String taskDefineKey, Integer handlerType);

    /**
     * 处理 ChildProcess 子流程的审批超时事件
     *
     * @param processInstanceId 流程示例编号
     * @param taskDefineKey     任务 Key
     */
    void processChildProcessTimeout(String processInstanceId, String taskDefineKey);

    /**
     * 触发流程任务 (ReceiveTask) 的执行
     * <p>
     * 1. Simple 模型 HTTP 回调请求触发器节点的回调，触发流程继续执行
     * 2. Simple 模型延迟器节点，到时触发流程继续执行
     *
     * @param processInstanceId 流程示例编号
     * @param taskDefineKey     任务 Key
     */
    void triggerTask(String processInstanceId, String taskDefineKey);

}
