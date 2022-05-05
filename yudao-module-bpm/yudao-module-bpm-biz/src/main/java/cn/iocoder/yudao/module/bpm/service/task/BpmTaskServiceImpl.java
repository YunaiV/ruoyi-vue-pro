package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.convert.task.BpmTaskConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmTaskAssignRuleMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.task.BpmTaskExtMapper;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceResultEnum;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 流程任务实例 Service 实现类
 *
 * @author 芋道源码
 * @author jason
 */
@Slf4j
@Service
public class BpmTaskServiceImpl implements BpmTaskService{

    @Resource
    private TaskService taskService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;

    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private BpmTaskExtMapper taskExtMapper;
    @Resource
    private BpmMessageService messageService;
    @Resource
    private BpmTaskAssignRuleMapper taskAssignRuleMapper;

    @Override
    public PageResult<BpmTaskTodoPageItemRespVO> getTodoTaskPage(Long userId, BpmTaskTodoPageReqVO pageVO) {
        // 查询待办任务
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId)) // 分配给自己
                .orderByTaskCreateTime().desc(); // 创建时间倒序
        if (StrUtil.isNotBlank(pageVO.getName())) {
            taskQuery.taskNameLike("%" + pageVO.getName() + "%");
        }
        if (pageVO.getBeginCreateTime() != null) {
            taskQuery.taskCreatedAfter(pageVO.getBeginCreateTime());
        }
        if (pageVO.getEndCreateTime() != null) {
            taskQuery.taskCreatedBefore(pageVO.getEndCreateTime());
        }
        // 执行查询
        List<Task> tasks = taskQuery.listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        if (CollUtil.isEmpty(tasks)) {
            return PageResult.empty(taskQuery.count());
        }

        // 获得 ProcessInstance Map
        Map<String, ProcessInstance> processInstanceMap = processInstanceService.getProcessInstanceMap(
                convertSet(tasks, Task::getProcessInstanceId));
        // 获得 User Map
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(processInstanceMap.values(), instance -> Long.valueOf(instance.getStartUserId())));
        // 拼接结果
        return new PageResult<>(BpmTaskConvert.INSTANCE.convertList1(tasks, processInstanceMap, userMap),
                taskQuery.count());
    }

    @Override
    public PageResult<BpmTaskDonePageItemRespVO> getDoneTaskPage(Long userId, BpmTaskDonePageReqVO pageVO) {
        // 查询已办任务
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                .finished() // 已完成
                .taskAssignee(String.valueOf(userId)) // 分配给自己
                .orderByHistoricTaskInstanceEndTime().desc(); // 审批时间倒序
        if (StrUtil.isNotBlank(pageVO.getName())) {
            taskQuery.taskNameLike("%" + pageVO.getName() + "%");
        }
        if (pageVO.getBeginCreateTime() != null) {
            taskQuery.taskCreatedAfter(pageVO.getBeginCreateTime());
        }
        if (pageVO.getEndCreateTime() != null) {
            taskQuery.taskCreatedBefore(pageVO.getEndCreateTime());
        }
        // 执行查询
        List<HistoricTaskInstance> tasks = taskQuery.listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        if (CollUtil.isEmpty(tasks)) {
            return PageResult.empty(taskQuery.count());
        }

        // 获得 TaskExtDO Map
        List<BpmTaskExtDO> bpmTaskExtDOs = taskExtMapper.selectListByTaskIds(convertSet(tasks, HistoricTaskInstance::getId));
        Map<String, BpmTaskExtDO> bpmTaskExtDOMap = convertMap(bpmTaskExtDOs, BpmTaskExtDO::getTaskId);
        // 获得 ProcessInstance Map
        Map<String, HistoricProcessInstance> historicProcessInstanceMap = processInstanceService.getHistoricProcessInstanceMap(
                convertSet(tasks, HistoricTaskInstance::getProcessInstanceId));
        // 获得 User Map
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(historicProcessInstanceMap.values(), instance -> Long.valueOf(instance.getStartUserId())));
        // 拼接结果
        return new PageResult<>(BpmTaskConvert.INSTANCE.convertList2(tasks, bpmTaskExtDOMap, historicProcessInstanceMap, userMap),
                taskQuery.count());
    }

    @Override
    public List<Task> getTasksByProcessInstanceIds(List<String> processInstanceIds) {
        if (CollUtil.isEmpty(processInstanceIds)) {
            return Collections.emptyList();
        }
        return taskService.createTaskQuery().processInstanceIdIn(processInstanceIds).list();
    }

    @Override
    public List<BpmTaskRespVO> getTaskListByProcessInstanceId(String processInstanceId) {
        // 获得任务列表
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().desc() // 创建时间倒序
                .list();
        if (CollUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }

        // 获得 TaskExtDO Map
        List<BpmTaskExtDO> bpmTaskExtDOs = taskExtMapper.selectListByTaskIds(convertSet(tasks, HistoricTaskInstance::getId));
        Map<String, BpmTaskExtDO> bpmTaskExtDOMap = convertMap(bpmTaskExtDOs, BpmTaskExtDO::getTaskId);
        // 获得 ProcessInstance Map
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(processInstanceId);
        // 获得 User Map
        Set<Long> userIds = convertSet(tasks, task -> NumberUtils.parseLong(task.getAssignee()));
        userIds.add(NumberUtils.parseLong(processInstance.getStartUserId()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 获得 Dept Map
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));

        // 拼接数据
        return BpmTaskConvert.INSTANCE.convertList3(tasks, bpmTaskExtDOMap, processInstance, userMap, deptMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(Long userId, @Valid BpmTaskApproveReqVO reqVO) {
        // 校验任务存在
        Task task = checkTask(userId, reqVO.getId());
        // 校验流程实例存在
        ProcessInstance instance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 完成任务，审批通过
        taskService.complete(task.getId(), instance.getProcessVariables());

        // 更新任务拓展表为通过
        taskExtMapper.updateByTaskId(
            new BpmTaskExtDO().setTaskId(task.getId()).setResult(BpmProcessInstanceResultEnum.APPROVE.getResult())
                .setComment(reqVO.getComment()));
        // 判断任务是否为或签，或签时删除其余不用审批的任务
        List<BpmTaskAssignRuleDO> bpmTaskAssignRuleList =
            taskAssignRuleMapper.selectListByProcessDefinitionId(task.getProcessDefinitionId(),
                task.getTaskDefinitionKey());
        if (CollUtil.isNotEmpty(bpmTaskAssignRuleList) && bpmTaskAssignRuleList.size() > 0) {
            if (BpmTaskAssignRuleTypeEnum.USER_OR_SIGN.getType().equals(bpmTaskAssignRuleList.get(0).getType())) {
                taskExtMapper.updateUserOrSignTask(
                    (BpmTaskExtDO)new BpmTaskExtDO().setTaskId(task.getId()).setName(task.getName())
                        .setProcessInstanceId(task.getProcessInstanceId()).setDeleted(true));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(Long userId, @Valid BpmTaskRejectReqVO reqVO) {
        Task task = checkTask(userId, reqVO.getId());
        // 校验流程实例存在
        ProcessInstance instance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 更新流程实例为不通过
        processInstanceService.updateProcessInstanceExtReject(instance.getProcessInstanceId(), reqVO.getComment());

        // 更新任务拓展表为不通过
        taskExtMapper.updateByTaskId(new BpmTaskExtDO().setTaskId(task.getId())
                .setResult(BpmProcessInstanceResultEnum.REJECT.getResult()).setComment(reqVO.getComment()));
    }

    @Override
    public void backTask(String taskId,String destinationTaskDefKey) {
        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(currentTask.getProcessInstanceId())
                .moveActivityIdTo(currentTask.getTaskDefinitionKey(), destinationTaskDefKey)
                .changeState();
    }

    @Override
    public void updateTaskAssignee(Long userId, BpmTaskUpdateAssigneeReqVO reqVO) {
        // 校验任务存在
        Task task = checkTask(userId, reqVO.getId());
        // 更新负责人
        updateTaskAssignee(task.getId(), reqVO.getAssigneeUserId());
    }

    @Override
    public void updateTaskAssignee(String id, Long userId) {
        taskService.setAssignee(id, String.valueOf(userId));
    }


    @Override
    public void createTaskExt(Task task) {
        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert2TaskExt(task)
                .setResult(BpmProcessInstanceResultEnum.PROCESS.getResult());
        taskExtMapper.insert(taskExtDO);
    }

    @Override
    public void updateTaskExtComplete(Task task) {
        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert2TaskExt(task)
                .setEndTime(new Date());
        taskExtMapper.updateByTaskId(taskExtDO);
    }

    @Override
    public void updateTaskExtAssign(Task task) {
        BpmTaskExtDO taskExtDO = new BpmTaskExtDO()
                .setAssigneeUserId(NumberUtils.parseLong(task.getAssignee()))
                .setTaskId(task.getId());
        taskExtMapper.updateByTaskId(taskExtDO);
        // 发送通知。在事务提交时，批量执行操作，所以直接查询会无法查询到 ProcessInstance，所以这里是通过监听事务的提交来实现。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ProcessInstance processInstance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
                AdminUserRespDTO startUser = adminUserApi.getUser(Long.valueOf(processInstance.getStartUserId()));
                messageService.sendMessageWhenTaskAssigned(BpmTaskConvert.INSTANCE.convert(processInstance, startUser, task));
            }
        });
    }

    /**
     * 校验任务是否存在， 并且是否是分配给自己的任务
     * @param userId 用户 id
     * @param taskId task id
     */
    private Task checkTask(Long userId, String taskId) {
        Task task = getTask(taskId);
        if (task == null) {
            throw exception(TASK_COMPLETE_FAIL_NOT_EXISTS);
        }
        if (!Objects.equals(userId, NumberUtils.parseLong(task.getAssignee()))) {
            throw exception(TASK_COMPLETE_FAIL_ASSIGN_NOT_SELF);
        }
        return task;
    }

    private Task getTask(String id) {
        return taskService.createTaskQuery().taskId(id).singleResult();
    }
}
