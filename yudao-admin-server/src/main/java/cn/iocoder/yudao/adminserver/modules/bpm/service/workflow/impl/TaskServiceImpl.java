package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.*;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow.TaskConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.TaskService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.ClaimTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
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
import org.springframework.util.CollectionUtils;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

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
            definition.getId();
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


    /**
     * 工作流，完成 userTask, 完成用户任务 一般传入参数 1。是否同意（variables).  2. 评论(comment)
     * variables 变量名 和 评论 由前台传入
     * @param taskReq 任务参数
     */
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
        // 转换结果
        TodoTaskRespVO respVO = new TodoTaskRespVO();
        respVO.setFormKey(task.getFormKey());
        respVO.setBusinessKey(task.getBusinessKey());
        respVO.setId(task.getId());
        respVO.setProcessInstanceId(task.getProcessInstanceId());
        return respVO;
    }


    @Override
    public void getHighlightImg(String processInstanceId, HttpServletResponse response) {
        // 查询历史
        //TODO 云扬四海 貌似流程结束后，点击审批进度会报错
        // TODO @Li：一些 historyService 的查询，貌似比较通用，是不是抽一些小方法出来
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        // 如果有结束时间 TODO @Li：如果查询不到，是不是抛出一个业务异常比较好哈？
        if (hpi == null) {
            return;
        }
        // 没有结束时间。说明流程在执行过程中
        // TODO @Li：一些 runtimeService 的查询，貌似比较通用，是不是抽一些小方法出来
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId()); // TODO @Li：这块和下面的逻辑比较相关，可以在后面一点查询。
        List<String> highLightedActivities = new ArrayList<>();

        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceId().asc().list(); // TODO @Li：这块和下面的逻辑比较相关，可以在后面一点查询。
        // 获取所有活动节点
        List<HistoricActivityInstance> finishedInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).finished().list();
        // TODO @Li：highLightedActivities 结果，可以使用 CollUtils.buildList() 方法。即使不用，也应该用 stream。简洁很重要。
        for (HistoricActivityInstance hai : finishedInstances) {
            highLightedActivities.add(hai.getActivityId());
        }
        // 已完成的节点+当前节点
        highLightedActivities.addAll(runtimeService.getActiveActivityIds(processInstanceId));

        // 经过的流
        List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstances);

        //设置"宋体"
        // TODO @Li：Service 返回 bytes，最终 Controller 去写
        try (InputStream inputStream = processDiagramGenerator.generateDiagram(bpmnModel, highLightedActivities, highLightedFlowIds,
                "宋体", "宋体", "宋体")){
            String picName = hpi.getProcessDefinitionName()+".svg";
            // 输出到浏览器
            responseImage(response, inputStream, picName);
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    // TODO @Li：参考 ServletUtils 方法。如果没有满足的，可以在写一个。
    private void responseImage(HttpServletResponse response, InputStream inputStream, String picName) throws IOException {
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(picName, "UTF-8"));
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
        response.flushBuffer();
    }

    // TODO @Li：这个方法的可读性还有一定的优化空间，可以思考下哈。
    /**
     * 获取已经流转的线 https://blog.csdn.net/qiuxinfa123/article/details/119579863
     * @see
     * @param bpmnModel model
     * @param historicActivityInstances 高亮线条
     * @return
     */
    private List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
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

        // TODO @Li：这两个变量，直接放到循环里。这种优化一般不需要做的，对性能影响超级小。
        FlowNode currentFlowNode;
        FlowNode targetFlowNode;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

            // 遍历outgoingFlows并找到已流转的 满足如下条件认为已已流转：
            // 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
            // 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
            // TODO @Li：“parallelGateway” 和 "inclusiveGateway"，有对应的枚举么？如果木有，可以自己枚举哈
            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        highLightedFlowIds.add(targetFlowNode.getId());
                    }
                }
            } else {
                // TODO @Li：如果是为了获取到时间更早的一个，是不是遍历的过程中，就可以解决
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("highLightedFlowId", sequenceFlow.getId());
                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                            tempMapList.add(map);
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(tempMapList)) {
                    // 遍历匹配的集合，取得开始时间最早的一个
                    long earliestStamp = 0L;
                    String highLightedFlowId = null;
                    for (Map<String, Object> map : tempMapList) {
                        // TODO @Li：可以使用 MapUtil 去 get 值
                        long highLightedFlowStartTime = Long.valueOf(map.get("highLightedFlowStartTime").toString());
                        if (earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime) {
                            highLightedFlowId = map.get("highLightedFlowId").toString();
                            earliestStamp = highLightedFlowStartTime;
                        }
                    }
                    highLightedFlowIds.add(highLightedFlowId);
                }

            }

        }
        return highLightedFlowIds;
    }
}
