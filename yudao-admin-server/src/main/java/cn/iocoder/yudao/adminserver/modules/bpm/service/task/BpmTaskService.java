package cn.iocoder.yudao.adminserver.modules.bpm.service.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.List;

/**
 * 工作流用户任务服务接口
 */
public interface BpmTaskService {


    /**
     * 获取当前用户的待办任务， 分页
     */
    PageResult<TodoTaskRespVO> getTodoTaskPage(TodoTaskPageReqVO pageReqVO);

    /**
     * 签收任务
     * @param taskId  用户任务id
     */
    void claimTask(String taskId);

    /**
     * 工作流，完成 userTask, 完成用户任务 一般传入参数 1。是否同意（variables).  2. 评论(comment)
     * variables 变量名 和 评论 由前台传入
     * @param taskReq 任务参数
     */
    void completeTask(TaskReqVO taskReq);

    /**
     * 根据任务id, 查询已经完成的用户任务，未完成的用户任务
     * @param taskQuery 查询参数  一般 taskId
     */
    TaskHandleVO getTaskSteps(TaskQueryReqVO taskQuery);

    /**
     * 根据流程实例id, 查询历史用户任务，包括已完成，未完成
     * @param processInstanceId 流程实例id
     */
    List<TaskStepVO> getHistorySteps(String processInstanceId);

    /**
     * 获取用户任务的 formKey, 对应外置表单， 需要根据formKey 对应业务表单
     * @param taskQuery 查询参数 ,一般taskId
     */
    TodoTaskRespVO getTaskFormKey(TaskQueryReqVO taskQuery);


    /**
     * 返回高亮的流转进程
     * @param processInstanceId 实例Id
     * @return {@link FileResp} 返回文件
     */
    FileResp getHighlightImg(String processInstanceId);
}
