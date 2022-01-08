package cn.iocoder.yudao.adminserver.modules.bpm.service.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.activiti.engine.task.Task;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 流程任务 Service 接口
 *
 * @author jason
 * @author 芋道源码
 */
public interface BpmTaskService {

    /**
     * 获得流程任务列表
     *
     * @param processInstanceId 流程实例的编号
     * @return 流程任务列表
     */
    List<Task> getTasksByProcessInstanceId(String processInstanceId);

    /**
     * 获得流程任务列表
     *
     * @param processInstanceIds 流程实例的编号数组
     * @return 流程任务列表
     */
    List<Task> getTasksByProcessInstanceIds(List<String> processInstanceIds);

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
     * 获得待办的流程任务分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页请求
     * @return 流程任务分页
     */
    PageResult<BpmTaskTodoPageItemRespVO> getTodoTaskPage(Long userId, BpmTaskTodoPageReqVO pageReqVO);

    /**
     * 获得已办的流程任务分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页请求
     * @return 流程任务分页
     */
    PageResult<BpmTaskDonePageItemRespVO> getDoneTaskPage(Long userId, BpmTaskDonePageReqVO pageReqVO);

    /**
     * 将流程任务分配给指定用户
     *
     * @param id 流程任务编号
     * @param userId 用户编号
     */
    void updateTaskAssign(String id, Long userId);

    /**
     * 完成任务（审批通过 / 不通过）
     *
     * @param taskReq 完成请求
     */
    void completeTask(@Valid BpmTaskCompleteReqVO taskReq);

    /**
     * 根据任务id, 查询已经完成的用户任务，未完成的用户任务
     * @param taskQuery 查询参数  一般 taskId
     */
    @Deprecated
    TaskHandleVO getTaskSteps(TaskQueryReqVO taskQuery);

    /**
     * 根据流程实例id, 查询历史用户任务，包括已完成，未完成
     * @param processInstanceId 流程实例id
     */
    @Deprecated
    List<TaskStepVO> getHistorySteps(String processInstanceId);

    /**
     * 返回高亮的流转进程
     * @param processInstanceId 实例Id
     * @return {@link FileResp} 返回文件
     */
    FileResp getHighlightImg(String processInstanceId);

}
