package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.*;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow.TaskConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmTaskService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.ClaimTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.HIGHLIGHT_IMG_ERROR;
import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Slf4j
@Service
public class BpmTaskServiceImpl implements BpmTaskService {

    @Resource
    private  TaskRuntime taskRuntime;

    @Resource
    private org.activiti.engine.TaskService activitiTaskService;

    @Resource
    private HistoryService  historyService;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private ProcessDiagramGenerator processDiagramGenerator;

    @Override
    public PageResult<TodoTaskRespVO> getTodoTaskPage(TodoTaskPageReqVO pageReqVO) {
        // TODO @jason：封装一个方法，用于转换成 activiti 的分页对象
        final Pageable pageable = Pageable.of((pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize(), pageReqVO.getPageSize());
        Page<Task> pageTasks = taskRuntime.tasks(pageable);
        int totalItems = pageTasks.getTotalItems();
        List<Task> tasks = pageTasks.getContent();
        final List<TodoTaskRespVO> respVOList = tasks.stream().map(task -> {
            ProcessDefinition definition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
            return  TaskConvert.INSTANCE.convert(task, definition);
        }).collect(Collectors.toList());
        return new PageResult<>(respVOList, (long)totalItems);
    }


    @Override
    public void claimTask(String taskId) {
        taskRuntime.claim(new ClaimTaskPayloadBuilder()
                                .withTaskId(taskId)
                                .withAssignee(SecurityFrameworkUtils.getLoginUser().getUsername())
                                .build());
    }



    @Override
    @Transactional
    public void completeTask(TaskReqVO taskReq) {
        final Task task = taskRuntime.task(taskReq.getTaskId());

        activitiTaskService.addComment(taskReq.getTaskId(), task.getProcessInstanceId(), taskReq.getComment());

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(taskReq.getTaskId())
                .withVariables(taskReq.getVariables())
                .build());
    }



    @Override
    public TaskHandleVO getTaskSteps(TaskQueryReqVO taskQuery) {
        TaskHandleVO handleVO = new TaskHandleVO();
        final Task task = taskRuntime.task(taskQuery.getTaskId());
        List<TaskStepVO> steps = getTaskSteps(task.getProcessInstanceId());
        handleVO.setHistoryTask(steps);
        return handleVO;
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
            TaskStepVO stepVO = TaskConvert.INSTANCE.convert(instance);
            stepVO.setStatus(1); // TODO @jason：1 这个 magic number 要枚举起来。
            // TODO @jason：可以考虑把 comments 读取后，在统一调用 convert 拼接。另外 Comment 是废弃的类，有没其它可以使用的哈？
            List<Comment> comments = activitiTaskService.getTaskComments(instance.getTaskId());
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
            TaskStepVO stepVO = TaskConvert.INSTANCE.convert(instance);
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
    public TodoTaskRespVO getTaskFormKey(TaskQueryReqVO taskQuery) {
        final Task task = taskRuntime.task(taskQuery.getTaskId());
        return TaskConvert.INSTANCE.convert(task);
    }

    @Override
    public FileResp getHighlightImg(String processInstanceId) {
        // 查询历史
        //TODO 云扬四海 貌似流程结束后，点击审批进度会报错
        // TODO @Li：一些 historyService 的查询，貌似比较通用，是不是抽一些小方法出来
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        // 如果不存在实例。 说明数据异常
        if (hpi == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
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
            String picName = hpi.getProcessDefinitionName() + ".svg"; // TODO @Li：一次性的变量，可以直接 set 的时候，直接拼接
            fileResp.setFileName(picName);
            fileResp.setFileByte(IoUtil.readBytes(inputStream));
            return fileResp;
        } catch (IOException e) {
            // TODO @Li：log.error("[getHighlightImg][流程({}) 生成图表失败]", processInstanceId, e)
            log.error(ExceptionUtils.getStackTrace(e));
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
}
