package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

// TODO @jason：前缀
/**
 * 用户任务服务接口
 */
public interface TaskService {


    /**
     * 获取当前用户的待办任务， 分页
     */
    PageResult<TodoTaskRespVO> getTodoTaskPage(TodoTaskPageReqVO pageReqVO);


    /**
     * 签收任务
     * @param taskId  用户任务id
     */
    void claimTask(String taskId);

    // TODO @jason：可以把实现方法的注释，统一写到接口里
    /**
     * 办理完成用户任务
     * @param taskReq 任务参数， 包含任务的参数，和 评论
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
     */
    FileResp getHighlightImg(String processInstanceId);
}
