package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
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
     * @return 流程任务分页
     */
    PageResult<BpmTaskTodoPageItemRespVO> getTodoTaskPage(Long userId, BpmTaskTodoPageReqVO pageReqVO);

    /**
     * 获得已办的流程任务分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页请求
     * @return 流程任务分页
     */
    PageResult<BpmTaskDonePageItemRespVO> getDoneTaskPage(Long userId, BpmTaskDonePageReqVO pageReqVO);

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
     * 获得指令流程实例的流程任务列表，包括所有状态的
     *
     * @param processInstanceId 流程实例的编号
     * @return 流程任务列表
     */
    List<BpmTaskRespVO> getTaskListByProcessInstanceId(String processInstanceId);


    /**
     * 通过任务 ID 集合，获取任务扩展表信息集合
     *
     * @param taskIdList 任务 ID 集合
     * @return 任务列表
     */
    List<BpmTaskExtDO> getTaskListByTaskIdList(List<String> taskIdList);

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

    /**
     * 获取当前任务的可回退的流程集合
     *
     * @param taskId 当前的任务 ID
     * @return 可以回退的节点列表
     */
    List<BpmTaskSimpleRespVO> getReturnTaskList(String taskId);

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
    void createSignTask(Long userId, BpmTaskAddSignReqVO reqVO);

    /**
     * 任务减签名
     *
     * @param userId 当前用户ID
     * @param reqVO  被减签的任务 ID，理由
     */
    void deleteSignTask(Long userId, BpmTaskSubSignReqVO reqVO);

    /**
     * 获取指定任务的子任务和审批人信息
     *
     * @param parentId 指定任务ID
     * @return 子任务列表
     */
    List<BpmTaskSubSignRespVO> getChildrenTaskList(String parentId);

}
