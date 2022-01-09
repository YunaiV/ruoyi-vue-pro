package cn.iocoder.yudao.adminserver.modules.bpm.service.task.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task.*;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.task.BpmTaskConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.task.BpmTaskExtMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.BpmProcessInstanceResultEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 流程任务 Service 实现类
 *
 * @author jason
 * @author 芋道源码
 */
@Slf4j
@Service
public class BpmTaskServiceImpl implements BpmTaskService {

    @Resource
    private TaskService taskService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService  historyService;
    @Resource
    private RepositoryService repositoryService;

    @Resource
    private ProcessDiagramGenerator processDiagramGenerator;

    @Resource
    private SysUserService userService;
    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessInstanceService processInstanceService;

    @Resource
    private BpmTaskExtMapper taskExtMapper;

    @Override
    public List<Task> getTasksByProcessInstanceId(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public List<Task> getTasksByProcessInstanceIds(List<String> processInstanceIds) {
        if (CollUtil.isEmpty(processInstanceIds)) {
            return Collections.emptyList();
        }
        return taskService.createTaskQuery().processInstanceIdIn(processInstanceIds).list();
    }

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
        Map<Long, SysUserDO> userMap = userService.getUserMap(
                convertSet(processInstanceMap.values(), instance -> Long.valueOf(instance.getStartUserId())));
        // 拼接结果
        return new PageResult<>(BpmTaskConvert.INSTANCE.convertList(tasks, processInstanceMap, userMap),
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

        // 获得 ProcessInstance Map
        Map<String, HistoricProcessInstance> historicProcessInstanceMap = processInstanceService.getHistoricProcessInstanceMap(
                convertSet(tasks, HistoricTaskInstance::getProcessInstanceId));
        // 获得 User Map
        Map<Long, SysUserDO> userMap = userService.getUserMap(
                convertSet(historicProcessInstanceMap.values(), instance -> Long.valueOf(instance.getStartUserId())));
        // 拼接结果
        return new PageResult<>(BpmTaskConvert.INSTANCE.convertList2(tasks, historicProcessInstanceMap, userMap),
                taskQuery.count());
    }

    @Override
    public void updateTaskAssign(String id, Long userId) {
        taskService.setAssignee(id, String.valueOf(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(BpmTaskApproveReqVO reqVO) {
        // 校验任务存在
        Task task = getTask(reqVO.getId());
        if (task == null) {
            throw exception(TASK_COMPLETE_FAIL_NOT_EXISTS);
        }
        // 校验流程实例存在
        ProcessInstance instance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 完成任务，审批通过
        taskService.complete(task.getId(), instance.getProcessVariables()); // TODO 芋艿：variables 的选择
        // 更新任务拓展表为通过
        taskExtMapper.updateByTaskId(new BpmTaskExtDO().setTaskId(task.getId())
                .setResult(BpmProcessInstanceResultEnum.APPROVE.getResult()).setComment(reqVO.getComment()));

        // TODO 芋艿：添加评论
//        taskService.addComment(task.getId(), task.getProcessInstanceId(), reqVO.getComment());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(@Valid BpmTaskRejectReqVO reqVO) {
        // 校验任务存在
        Task task = getTask(reqVO.getId());
        if (task == null) {
            throw exception(TASK_COMPLETE_FAIL_NOT_EXISTS);
        }
        // 校验流程实例存在
        ProcessInstance instance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 更新流程实例为不通过
        processInstanceService.updateProcessInstanceResult(instance.getProcessInstanceId(),
                BpmProcessInstanceResultEnum.REJECT.getResult());

        // 更新任务拓展表为不通过
        taskExtMapper.updateByTaskId(new BpmTaskExtDO().setTaskId(task.getId())
                .setResult(BpmProcessInstanceResultEnum.REJECT.getResult()).setComment(reqVO.getComment()));

        // TODO 芋艿：添加评论
//        taskService.addComment(task.getId(), task.getProcessInstanceId(), reqVO.getComment());
    }

    @Override
    public TaskHandleVO getTaskSteps(TaskQueryReqVO taskQuery) {
//        TaskHandleVO handleVO = new TaskHandleVO();
//        final Task task = taskRuntime.task(taskQuery.getTaskId());
//        List<TaskStepVO> steps = getTaskSteps(task.getProcessInstanceId());
//        handleVO.setHistoryTask(steps);
//        return handleVO;
        return null;
    }

    private List<TaskStepVO> getTaskSteps(String processInstanceId) {
        // 获得已完成的活动
        List<HistoricActivityInstance> finished = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceStartTime().asc().list();
        // 获得对应的步骤
        List<TaskStepVO> steps = new ArrayList<>();
        finished.forEach(instance -> {
            TaskStepVO stepVO = BpmTaskConvert.INSTANCE.convert(instance);
            stepVO.setStatus(1); // TODO @jason：1 这个 magic number 要枚举起来。
            // TODO @jason：可以考虑把 comments 读取后，在统一调用 convert 拼接。另外 Comment 是废弃的类，有没其它可以使用的哈？
            List<Comment> comments = taskService.getTaskComments(instance.getTaskId());
            if (!CollUtil.isEmpty(comments)) {
                stepVO.setComment(Optional.ofNullable(comments.get(0)).map(Comment::getFullMessage).orElse(""));
            }
            steps.add(stepVO);
        });

        // 获得未完成的活动
        List<HistoricActivityInstance> unfinished = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .unfinished().list();
        // 获得对应的步骤
        for (HistoricActivityInstance instance : unfinished) {
            TaskStepVO stepVO = BpmTaskConvert.INSTANCE.convert(instance);
            stepVO.setComment("");
            stepVO.setStatus(0);
            steps.add(stepVO);
        }
        return steps;
    }


    @Override
    public List<TaskStepVO> getHistorySteps(String processInstanceId) {
        return getTaskSteps(processInstanceId);
    }

    @Override
    public FileResp getHighlightImg(String processInstanceId) {
        // 查询历史
        //TODO 云扬四海 貌似流程结束后，点击审批进度会报错
        // TODO @Li：一些 historyService 的查询，貌似比较通用，是不是抽一些小方法出来
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        // 如果不存在实例。 说明数据异常
        if (hpi == null) {
//            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
            throw new RuntimeException("不存在");
        }
        // 如果有结束时间 返回model的流程图
        if (!ObjectUtils.isEmpty(hpi.getEndTime())) {
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(hpi.getProcessDefinitionId()).singleResult();
            String resourceName = Optional.ofNullable(pd.getDiagramResourceName()).orElse(pd.getResourceName());
            BpmnModel bpmnModel = repositoryService.getBpmnModel(pd.getId());
            InputStream inputStream = processDiagramGenerator.generateDiagram(bpmnModel, new ArrayList<>(1), new ArrayList<>(1),
                    "宋体", "宋体", "宋体");
            FileResp fileResp = new FileResp();
            fileResp.setFileName( resourceName + ".svg");
            fileResp.setFileByte(IoUtil.readBytes(inputStream));
            return fileResp;
        }
        // 没有结束时间。说明流程在执行过程中
        // TODO @Li：一些 runtimeService 的查询，貌似比较通用，是不是抽一些小方法出来
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        List<String> highLightedActivities = new ArrayList<>();
        // 获取所有活动节点
        List<HistoricActivityInstance> finishedInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).finished().list();
        finishedInstances.stream().map(HistoricActivityInstance::getActivityId)
                .forEach(highLightedActivities::add);
        // 已完成的节点+当前节点
        highLightedActivities.addAll(runtimeService.getActiveActivityIds(processInstanceId));

        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        // 经过的流
        List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, processInstanceId);

        //设置"宋体"
        try (InputStream inputStream = processDiagramGenerator.generateDiagram(bpmnModel, highLightedActivities, highLightedFlowIds,
                "宋体", "宋体", "宋体")){
            FileResp fileResp = new FileResp();
            fileResp.setFileName( hpi.getProcessDefinitionName() + ".svg");
            fileResp.setFileByte(IoUtil.readBytes(inputStream));
            return fileResp;
        } catch (IOException e) {
            log.error("[getHighlightImg][流程({}) 生成图表失败]", processInstanceId, e);
            throw exception(HIGHLIGHT_IMG_ERROR);
        }
    }

    // TODO @Li：这个方法的可读性还有一定的优化空间，可以思考下哈。
    /**
     * 获取指定 processInstanceId 已经高亮的Flows
     * 获取已经流转的线 参考： https://blog.csdn.net/qiuxinfa123/article/details/119579863
     * @param bpmnModel model
     * @param processInstanceId 流程实例Id
     * @return 获取已经流转的列表
     */
    private List<String> getHighLightedFlows(BpmnModel bpmnModel, String processInstanceId) {
        // 获取所有的线条
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceId().asc().list();
        // 高亮流程已发生流转的线id集合
        List<String> highLightedFlowIds = new ArrayList<>();
        // 全部活动节点
        List<FlowNode> historicActivityNodes = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            historicActivityNodes.add(flowNode);
            // 结束时间不为空，则是已完成节点
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstances.add(historicActivityInstance);
            }
        }
        // 提取活动id 是唯一的。塞入Map
        Map<String, HistoricActivityInstance> historicActivityInstanceMap = CollectionUtils.convertMap(historicActivityInstances, HistoricActivityInstance::getActivityId);
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

            // 遍历outgoingFlows并找到已流转的 满足如下条件认为已已流转：
            // 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
            // 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
            if (BpmnXMLConstants.ELEMENT_GATEWAY_PARALLEL.equals(currentActivityInstance.getActivityType())
                    || BpmnXMLConstants.ELEMENT_GATEWAY_INCLUSIVE.equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    FlowNode targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        highLightedFlowIds.add(targetFlowNode.getId());
                    }
                }
            } else {
                long earliestStamp = 0L;
                String highLightedFlowId = null;
                // 循环流出的流
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    HistoricActivityInstance historicActivityInstance = historicActivityInstanceMap.get(sequenceFlow.getTargetRef());
                    if (historicActivityInstance == null) {
                        continue;
                    }
                    final long startTime = historicActivityInstance.getStartTime().getTime();
                    // 遍历匹配的集合，取得开始时间最早的一个
                    if (earliestStamp == 0 || earliestStamp >= startTime) {
                        highLightedFlowId = sequenceFlow.getId();
                        earliestStamp = startTime;
                    }
                }
                highLightedFlowIds.add(highLightedFlowId);
            }

        }
        return highLightedFlowIds;
    }

    private Task getTask(String id) {
        return taskService.createTaskQuery().taskId(id).singleResult();
    }

    // ========== Task 拓展表相关 ==========

    @Override
    public void createTaskExt(org.activiti.api.task.model.Task task) {
        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert(task)
                .setResult(BpmProcessInstanceResultEnum.PROCESS.getResult());
        taskExtMapper.insert(taskExtDO);
    }

    @Override
    public void updateTaskExt(org.activiti.api.task.model.Task task) {
        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert(task);
        taskExtMapper.updateByTaskId(taskExtDO);
    }

    @Override
    public void updateTaskExtCancel(org.activiti.api.task.model.Task task) {
        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert(task)
                .setEndTime(new Date()) // 由于 Task 里没有办法拿到 endTime，所以这里设置
                .setResult(BpmProcessInstanceResultEnum.CANCEL.getResult());
        taskExtMapper.updateByTaskId(taskExtDO);
    }

    @Override
    public void updateTaskExtComplete(org.activiti.api.task.model.Task task) {
        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert(task)
                .setEndTime(task.getCompletedDate())
                .setResult(BpmProcessInstanceResultEnum.APPROVE.getResult());
        taskExtMapper.updateByTaskId(taskExtDO);
    }

}
