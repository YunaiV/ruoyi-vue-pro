package cn.iocoder.yudao.adminserver.modules.activiti.service.workflow.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo.*;
import cn.iocoder.yudao.adminserver.modules.activiti.convert.workflow.TaskConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.service.workflow.TaskService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.ClaimTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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


    /**
     * 工作流，完成 userTask, 完成用户任务 一般传入参数 1。是否同意（variables).  2. 评论(comment)
     * variables 变量名 和 评论 由前台传入
     * @param taskReq
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

//    @Override
//    public void flowImage(String taskId, HttpServletResponse response) {
//
//        final Task task = taskRuntime.task(taskId);
//        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
//        final Process process = bpmnModel.getMainProcess();
//        ProcessDefinitionEntity processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
//        List<String> activeActivityIds = runtimeService.getActiveActivityIds(executionId);
//        List<String> highLightedFlows = getHighLightedFlows(processDefinition, processInstance.getId());
//        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
//        InputStream imageStream =diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds, highLightedFlows);
//
//        // 输出资源内容到相应对象
//        byte[] b = new byte[1024];
//        int len;
//        while ((len = imageStream.read(b, 0, 1024)) != -1) {
//            response.getOutputStream().write(b, 0, len);
//        }
//    }

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
                // TODO @jason：IDEA 在 t.getFullMessage() 有提示告警，可以解决下。
                stepVO.setComment(Optional.ofNullable(comments.get(0)).map(t->t.getFullMessage()).orElse(""));
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
        return respVO;
    }

//    private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId) {
//
//        List<String> highLightedFlows = new ArrayList<String>();
//        List<HistoricActivityInstance> historicActivityInstances = historyService
//                .createHistoricActivityInstanceQuery()
//                .processInstanceId(processInstanceId)
//                .orderByHistoricActivityInstanceStartTime().asc().list();
//
//        List<String> historicActivityInstanceList = new ArrayList<String>();
//        for (HistoricActivityInstance hai : historicActivityInstances) {
//            historicActivityInstanceList.add(hai.getActivityId());
//        }

//        // add current activities to list
//        List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
//        historicActivityInstanceList.addAll(highLightedActivities);

        // activities and their sequence-flows
//        for (ActivityImpl activity : processDefinition.getActivities()) {
//            int index = historicActivityInstanceList.indexOf(activity.getId());
//
//            if (index >= 0 && index + 1 < historicActivityInstanceList.size()) {
//                List<PvmTransition> pvmTransitionList = activity
//                        .getOutgoingTransitions();
//                for (PvmTransition pvmTransition : pvmTransitionList) {
//                    String destinationFlowId = pvmTransition.getDestination().getId();
//                    if (destinationFlowId.equals(historicActivityInstanceList.get(index + 1))) {
//                        highLightedFlows.add(pvmTransition.getId());
//                    }
//                }
//            }
//        }
//        return highLightedFlows;
//    }

}
