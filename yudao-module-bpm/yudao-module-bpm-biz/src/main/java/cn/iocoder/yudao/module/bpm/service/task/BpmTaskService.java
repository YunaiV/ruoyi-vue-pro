package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerTypeEnum;
import jakarta.validation.Valid;
import org.flowable.bpmn.model.UserTask;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 流程任务实例 Service 接口
 *
 * @author jason
 * @author 芋道源码
 */
public interface BpmTaskService {

    // ========== Query 查询相关方法 ==========

    /**
     * 获得待办的流程任务分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页请求
     * @return 流程任务分页
     */
    PageResult<Task> getTaskTodoPage(Long userId, BpmTaskPageReqVO pageReqVO);

    /**
     * 获得已办的流程任务分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页请求
     * @return 流程任务分页
     */
    PageResult<HistoricTaskInstance> getTaskDonePage(Long userId, BpmTaskPageReqVO pageReqVO);

    /**
     * 获得全部的流程任务分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页请求
     * @return 流程任务分页
     */
    PageResult<HistoricTaskInstance> getTaskPage(Long userId, BpmTaskPageReqVO pageReqVO);

    /**
     * 获得流程任务 Map
     *
     * @param processInstanceIds 流程实例的编号数组
     * @return 流程任务 Map
     */
    default Map<String, List<Task>> getTaskMapByProcessInstanceIds(List<String> processInstanceIds) {
        return CollectionUtils.convertMultiMap(getTasksByProcessInstanceIds(processInstanceIds),
                Task::getProcessInstanceId);
    }

    /**
     * 获得流程任务列表
     *
     * @param processInstanceIds 流程实例的编号数组
     * @return 流程任务列表
     */
    List<Task> getTasksByProcessInstanceIds(List<String> processInstanceIds);

    /**
     * 获得指定流程实例的流程任务列表，包括所有状态的
     *
     * @param processInstanceId 流程实例的编号
     * @return 流程任务列表
     */
    List<HistoricTaskInstance> getTaskListByProcessInstanceId(String processInstanceId);

    /**
     * 获取任务
     *
     * @param id 任务编号
     * @return 任务
     */
    Task getTask(String id);

    /**
     * 获取历史任务
     *
     * @param id 任务编号
     * @return 历史任务
     */
    HistoricTaskInstance getHistoricTask(String id);

    /**
     * 获取历史任务列表
     *
     * @param taskIds 任务编号集合
     * @return 历史任务列表
     */
    List<HistoricTaskInstance> getHistoricTasks(Collection<String> taskIds);

    /**
     * 根据条件查询正在进行中的任务
     *
     * @param processInstanceId 流程实例编号，不允许为空
     * @param assigned 是否分配了审批人，允许空
     * @param taskDefineKey 任务定义 Key，允许空
     */
    List<Task> getRunningTaskListByProcessInstanceId(String processInstanceId,
                                                     Boolean assigned,
                                                     String taskDefineKey);

    /**
     * 获取当前任务的可回退的 UserTask 集合
     *
     * @param id 当前的任务 ID
     * @return 可以回退的节点列表
     */
    List<UserTask> getUserTaskListByReturn(String id);

    /**
     * 获取指定任务的子任务列表
     *
     * @param parentTaskId 父任务ID
     * @return 子任务列表
     */
    List<Task> getTaskListByParentTaskId(String parentTaskId);

    /**
     * 通过任务 ID，查询任务名 Map
     *
     * @param taskIds 任务 ID
     * @return 任务 ID 与名字的 Map
     */
    Map<String, String> getTaskNameByTaskIds(Collection<String> taskIds);

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
     */
    void moveTaskToEnd(String processInstanceId);

    /**
     * 将任务回退到指定的 targetDefinitionKey 位置
     *
     * @param userId 用户编号
     * @param reqVO  回退的任务key和当前所在的任务ID
     */
    void returnTask(Long userId, BpmTaskReturnReqVO reqVO);

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

    // ========== Event 事件相关方法 ==========

    /**
     * 处理 Task 创建事件，目前是
     *
     * 1. 更新它的状态为审批中
     * 2. 处理自动通过的情况，例如说：1）无审批人时，是否自动通过、不通过；2）非【人工审核】时，是否自动通过、不通过
     *
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
     * 处理 Task 审批超时事件，可能会处理多个当前审批中的任务
     *
     * @param processInstanceId 流程示例编号
     * @param taskDefineKey 任务 Key
     * @param handlerType 处理类型，参见 {@link BpmUserTaskTimeoutHandlerTypeEnum}
     */
    void processTaskTimeout(String processInstanceId, String taskDefineKey, Integer handlerType);

}
