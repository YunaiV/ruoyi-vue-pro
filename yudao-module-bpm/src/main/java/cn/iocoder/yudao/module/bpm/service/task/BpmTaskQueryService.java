package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.enums.task.BpmAttachmentTypeEnum;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.task.Attachment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程任务实例【读】Service 接口
 *
 * @author 芋道源码
 */
public interface BpmTaskQueryService {

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
     * 获得用户（待办）的任务：
     * 1. 根据 taskId 查询待办任务
     * 2. 如果任务不存在（或者已审核），获取指定流程下，首个需要处理任务
     *
     * @param userId 用户编号
     * @param taskId 任务编号
     * @param processInstanceId 流程实例编号
     * @return 待办任务
     */
    BpmTaskRespVO getTodoTask(Long userId, String taskId, String processInstanceId);

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
     * @param asc               是否升序
     * @return 流程任务列表
     */
    List<HistoricTaskInstance> getTaskListByProcessInstanceId(String processInstanceId, Boolean asc);

    /**
     * 校验任务是否存在，并且是否是分配给自己的任务
     *
     * @param userId 用户 id
     * @param taskId task id
     */
    Task validateTask(Long userId, String taskId);

    /**
     * 校验任务是否存在
     *
     * @param id 任务编号
     * @return 任务
     */
    Task validateTaskExists(String id);

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
     * 获取历史任务 Map
     *
     * @param taskIds 任务编号集合
     * @return 历史任务 Map
     */
    default Map<String, HistoricTaskInstance> getHistoricTaskMap(Collection<String> taskIds) {
        return CollectionUtils.convertMap(getHistoricTasks(taskIds), HistoricTaskInstance::getId);
    }

    /**
     * 根据条件查询正在进行中的任务
     *
     * @param processInstanceId 流程实例编号，不允许为空
     * @param assigned          是否分配了审批人，允许空
     * @param taskDefineKey     任务定义 Key，允许空
     */
    List<Task> getRunningTaskListByProcessInstanceId(String processInstanceId,
                                                     Boolean assigned,
                                                     String taskDefineKey);

    /**
     * 获取当前任务的可退回的 UserTask 集合
     *
     * @param id 当前的任务 ID
     * @return 可以退回的节点列表
     */
    List<UserTask> getUserTaskListByReturn(String id);

    /**
     * 获取指定任务的子任务列表（多层）
     *
     * @param parentTaskId 父任务 ID
     * @param tasks 任务列表
     * @return 子任务列表
     */
    <T extends TaskInfo> List<T> getAllChildrenTaskListByParentTaskId(String parentTaskId, List<T> tasks);

    /**
     * 获取指定任务的子任务列表
     *
     * @param parentTaskId 父任务ID
     * @return 子任务列表
     */
    List<Task> getTaskListByParentTaskId(String parentTaskId);

    /**
     * 获得指定流程实例的活动实例列表
     *
     * @param processInstanceId 流程实例的编号
     * @return 活动实例列表
     */
    List<HistoricActivityInstance> getActivityListByProcessInstanceId(String processInstanceId);

    /**
     * 获得执行编号对应的活动实例
     *
     * @param executionId 执行编号
     * @return 活动实例
     */
    List<HistoricActivityInstance> getHistoricActivityListByExecutionId(String executionId);

    /**
     * 获得指定流程实例的已完成的流程任务列表，不包含取消状态
     *
     * @param processInstanceId 流程实例的编号
     * @return 流程任务列表
     */
    List<HistoricTaskInstance> getFinishedTaskListByProcessInstanceIdWithoutCancel(String processInstanceId);


    /**
     * 根据条件获取附件
     *
     * @param processInstanceId 流程 id
     * @param taskIds 任务 id 集合
     * @param attachmentType 附件类型
     * @return 附件集合
     */
    List<Attachment> getAttachments(String processInstanceId, Set<String> taskIds, BpmAttachmentTypeEnum attachmentType);

    /**
     * 获得指定用户的退回修改中（WAIT_REVISION）的任务
     *
     * @param userId            用户编号，即修改人
     * @param processInstanceId 流程实例编号；为空时，不限制流程实例
     * @param taskId            任务编号；为空时，返回该流程实例中，该用户首个待修改的任务
     * @return 任务，包含任务变量；不是该用户在当前租户下待修改的任务时，返回 null
     */
    Task getRevisionTask(Long userId, String processInstanceId, String taskId);

    /**
     * 获得指定用户的退回修改中（WAIT_REVISION）的任务分页
     *
     * @param userId 用户编号，即修改人
     * @param pageVO 分页请求
     * @return 任务分页，包含任务变量
     */
    PageResult<Task> getRevisionTaskPage(Long userId, BpmTaskPageReqVO pageVO);

}
