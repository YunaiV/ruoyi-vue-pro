package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceProgressRespVO.User;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmProcessNodeProgressEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.module.bpm.enums.definition.BpmProcessNodeProgressEnum.*;


/**
 * BPM 活动实例 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
@Validated
public class BpmActivityServiceImpl implements BpmActivityService {

    /**
     * 抄送节点活动类型
     */
    private static final String COPY_NODE_ACTIVITY_TYPE = "serviceTask";
    /**
     * 审批节点活动类型
     */
    private static final String APPROVE_NODE_ACTIVITY_TYPE = "userTask";

    @Resource
    private HistoryService historyService;
    @Resource
    @Lazy
    private BpmTaskService bpmTaskService;
    @Resource
    private BpmProcessInstanceCopyService bpmProcessInstanceCopyService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmTaskCandidateInvoker bpmTaskCandidateInvoker;

    @Override
    public List<HistoricActivityInstance> getActivityListByProcessInstanceId(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();
    }

    @Override
    public List<HistoricActivityInstance> getHistoricActivityListByExecutionId(String executionId) {
        return historyService.createHistoricActivityInstanceQuery().executionId(executionId).list();
    }

    // TODO @芋艿：重点在 review 下~
    @Override
    public List<User> getHistoricActivityUserList(HistoricActivityInstance historicActivity
            , Boolean isMultiInstance, List<HistoricActivityInstance> historicActivityList) {
        Assert.notNull(historicActivity, "historicActivity 不能为 null ");
        List<User> returnUserList = Collections.emptyList();
        if (COPY_NODE_ACTIVITY_TYPE.equals(historicActivity.getActivityType())) {
            Set<Long> copyUserIds = bpmProcessInstanceCopyService.getCopyUserIds(historicActivity.getProcessInstanceId(),
                    historicActivity.getActivityId());
            List<AdminUserRespDTO> userList = adminUserApi.getUserList(copyUserIds);
            returnUserList = CollectionUtils.convertList(userList, item -> {
                User user = BeanUtils.toBean(item, User.class);
                user.setProcessed(Boolean.TRUE);
                return user;
            });
        } else if (APPROVE_NODE_ACTIVITY_TYPE.equals(historicActivity.getActivityType())) {
            if (isMultiInstance) {  // 多人 (会签 、 或签） // TODO 依次审批可能要特殊处理一下
                // 多个任务列表
                List<HistoricActivityInstance> taskList = CollectionUtils.filterList(historicActivityList,
                        item -> historicActivity.getActivityId().equals(item.getActivityId()));
                List<Long> userIds = CollectionUtils.convertList(taskList, item -> NumberUtil.parseLong(item.getAssignee(), null));
                List<String> taskIds = CollectionUtils.convertList(taskList, HistoricActivityInstance::getTaskId);
                Map<Long, AdminUserRespDTO> adminUserMap = CollectionUtils.convertMap(adminUserApi.getUserList(userIds), AdminUserRespDTO::getId);
                Map<String, HistoricTaskInstance> historicTaskInstanceMap = CollectionUtils.convertMap(bpmTaskService.getHistoricTasks(taskIds), HistoricTaskInstance::getId);
                returnUserList = CollectionUtils.convertList(taskList, item -> {
                    AdminUserRespDTO adminUser = adminUserMap.get(NumberUtil.parseLong(item.getAssignee(), null));
                    User user = BeanUtils.toBean(adminUser, User.class);
                    if (user != null) {
                        HistoricTaskInstance taskInstance = historicTaskInstanceMap.get(item.getTaskId());
                        if (taskInstance != null) {
                            user.setProcessed(taskInstance.getEndTime() != null);
                            user.setUserTaskStatus(FlowableUtils.getTaskStatus(taskInstance));
                        }
                    }
                    return user;
                });
            } else {
                AdminUserRespDTO adminUserResp = adminUserApi.getUser(Long.valueOf(historicActivity.getAssignee()));
                if (adminUserResp != null) {
                    User user = BeanUtils.toBean(adminUserResp, User.class);
                    // TODO 需要处理加签
                    // 查询任务状态
                    HistoricTaskInstance historicTask = bpmTaskService.getHistoricTask(historicActivity.getTaskId());
                    if (historicTask != null) {
                        Integer taskStatus = FlowableUtils.getTaskStatus(historicTask);
                        user.setProcessed(historicTask.getEndTime() != null);
                        user.setUserTaskStatus(taskStatus);
                    }
                    returnUserList = ListUtil.of(user);
                }
            }
        }
        return returnUserList;
    }

    @Override
    public Integer getHistoricActivityProgressStatus(HistoricActivityInstance historicActivity
            , Boolean isMultiInstance, List<HistoricActivityInstance> historicActivityList) {
        Assert.notNull(historicActivity, "historicActivity 不能为 null ");
        Integer progressStatus = null;
        if (APPROVE_NODE_ACTIVITY_TYPE.equals(historicActivity.getActivityType())) {
            if (isMultiInstance) { // 多人 (会签 、 或签）
                // 多个任务列表
                List<HistoricActivityInstance> taskList = CollectionUtils.filterList(historicActivityList,
                        item -> historicActivity.getActivityId().equals(item.getActivityId()));
                List<String> taskIds = CollectionUtils.convertList(taskList, HistoricActivityInstance::getTaskId);
                Map<String, HistoricTaskInstance> historicTaskMap = CollectionUtils.convertMap(bpmTaskService.getHistoricTasks(taskIds), HistoricTaskInstance::getId);
                for (HistoricActivityInstance activity : taskList) {
                    if (activity.getEndTime() == null) {
                        progressStatus = RUNNING.getStatus();
                    } else {
                        HistoricTaskInstance task = historicTaskMap.get(activity.getTaskId());
                        if (task != null) {
                            Integer taskStatus = FlowableUtils.getTaskStatus(task);
                            progressStatus = BpmProcessNodeProgressEnum.convertBpmnTaskStatus(taskStatus);
                        }
                    }
                    // 运行中或者未通过状态。退出循环 (会签可能需要多人通过）
                    if (RUNNING.getStatus().equals(progressStatus) || isUserTaskNotApproved(progressStatus)) {
                        break;
                    }
                }
            } else {
                HistoricTaskInstance historicTask = bpmTaskService.getHistoricTask(historicActivity.getTaskId());
                if (historicTask != null) {
                    Integer taskStatus = FlowableUtils.getTaskStatus(historicTask);
                    progressStatus = BpmProcessNodeProgressEnum.convertBpmnTaskStatus(taskStatus);
                }
            }
        } else {
            if (historicActivity.getEndTime() == null) {
                progressStatus = RUNNING.getStatus();
            }else {
                progressStatus = BpmProcessNodeProgressEnum.FINISHED.getStatus();
            }

        }
        return progressStatus;
    }

    @Override
    public Integer getNotRunActivityProgressStatus(Integer processInstanceStatus) {
        if(BpmProcessInstanceStatusEnum.isProcessEndStatus(processInstanceStatus)){
            return SKIP.getStatus();
        }else {
            return NOT_START.getStatus();
        }
    }

    @Override
    public List<User> getNotRunActivityUserList(String processInstanceId, Integer processInstanceStatus, Integer candidateStrategy, String candidateParam) {
        if(BpmProcessInstanceStatusEnum.isProcessEndStatus(processInstanceStatus)){
            // 跳过节点。返回空
            return Collections.emptyList();
        }else {
            BpmTaskCandidateStrategy taskCandidateStrategy = bpmTaskCandidateInvoker.getCandidateStrategy(candidateStrategy);
            Set<Long> userIds = taskCandidateStrategy.calculateUsers(processInstanceId, candidateParam);
            List<AdminUserRespDTO> userList = adminUserApi.getUserList(userIds);
            return CollectionUtils.convertList(userList, item -> {
                User user = BeanUtils.toBean(item, User.class);
                user.setProcessed(Boolean.FALSE);
                return user;
            });
        }
    }

}
