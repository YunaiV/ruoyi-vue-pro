package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import org.flowable.task.api.Task;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 流程任务实例 Service 接口
 *
 * @author jason
 * @author 芋道源码
 */
public interface BpmTaskService {

    /**
     * 获得待办的流程任务分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页请求
     *
     * @return 流程任务分页
     */
    PageResult<BpmTaskTodoPageItemRespVO> getTodoTaskPage(Long userId, BpmTaskTodoPageReqVO pageReqVO);

    /**
     * 获得已办的流程任务分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页请求
     *
     * @return 流程任务分页
     */
    PageResult<BpmTaskDonePageItemRespVO> getDoneTaskPage(Long userId, BpmTaskDonePageReqVO pageReqVO);

    /**
     * 获得流程任务 Map
     *
     * @param processInstanceIds 流程实例的编号数组
     *
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
     *
     * @return 流程任务列表
     */
    List<Task> getTasksByProcessInstanceIds(List<String> processInstanceIds);

    /**
     * 获得指令流程实例的流程任务列表，包括所有状态的
     *
     * @param processInstanceId 流程实例的编号
     *
     * @return 流程任务列表
     */
    List<BpmTaskRespVO> getTaskListByProcessInstanceId(String processInstanceId);

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
    void updateTaskAssignee(Long userId, BpmTaskUpdateAssigneeReqVO reqVO);

    /**
     * 将流程任务分配给指定用户
     *
     * @param id     流程任务编号
     * @param userId 用户编号
     */
    void updateTaskAssignee(String id, Long userId);

    /**
     * 创建 Task 拓展记录
     *
     * @param task 任务实体
     */
    void createTaskExt(Task task);

    /**
     * 更新 Task 拓展记录为完成
     *
     * @param task 任务实体
     */
    void updateTaskExtComplete(Task task);

    /**
     * 更新 Task 拓展记录为已取消
     *
     * @param taskId 任务的编号
     */
    void updateTaskExtCancel(String taskId);

    /**
     * 更新 Task 拓展记录，并发送通知
     *
     * @param task 任务实体
     */
    void updateTaskExtAssign(Task task);

}
