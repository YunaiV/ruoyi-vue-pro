package cn.iocoder.yudao.adminserver.modules.activiti.service.workflow.impl;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo.*;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.oa.OaLeaveMapper;
import cn.iocoder.yudao.adminserver.modules.activiti.service.workflow.TaskService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.google.common.collect.ImmutableMap;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.ClaimTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Resource
    private OaLeaveMapper leaveMapper;

    private static Map<String,String>  taskVariable =  ImmutableMap.<String,String>builder()
                    .put("deptLeaderVerify","deptLeaderApproved")
                    .put("hrVerify","hrApproved")
                    .build();

    public TaskServiceImpl() {

    }

    @Override
    public PageResult<TodoTaskRespVO> getTodoTaskPage(TodoTaskPageReqVO pageReqVO) {
        final LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        // TODO @jason：封装一个方法，用于转换成 activiti 的分页对象
        final Pageable pageable = Pageable.of((pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize(), pageReqVO.getPageSize());
        Page<Task> pageTasks = taskRuntime.tasks(pageable);
        // TODO @jason：convert 里转换
        List<Task> tasks = pageTasks.getContent();
        int totalItems = pageTasks.getTotalItems();
        final List<TodoTaskRespVO> respVOList = tasks.stream().map(task -> {
            TodoTaskRespVO respVO = new TodoTaskRespVO();
            respVO.setId(task.getId());
            final ProcessDefinition definition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
            respVO.setProcessName(definition.getName());
            respVO.setProcessKey(definition.getKey());
            respVO.setBusinessKey(task.getBusinessKey());
            respVO.setStatus(task.getAssignee() == null ? 1 : 2);
            return respVO;
        }).collect(Collectors.toList());
        // TODO @jason：要注意泛型哈。
        return new PageResult(respVOList, Long.valueOf(totalItems)); // TODO @jason：(long) 转换即可
    }


    @Override
    public void claimTask(String taskId) {
        taskRuntime.claim(new ClaimTaskPayloadBuilder()
                                .withTaskId(taskId)
                                .withAssignee(SecurityFrameworkUtils.getLoginUser().getUsername())
                                .build());
    }

    @Override
    public void getTaskHistory(String taskId) {
        final List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().
                processInstanceId("8e2801fc-1a38-11ec-98ce-74867a13730f").list();
    }

    // TODO @jason：一个方法里，会有多个方法的调用，最好写下对应的注释。这样容易理解
    @Override
    @Transactional
    public void completeTask(TaskReqVO taskReq) {
        final Task task = taskRuntime.task(taskReq.getTaskId());

        final Map<String, Object> variables = taskReq.getVariables();

        activitiTaskService.addComment(taskReq.getTaskId(), task.getProcessInstanceId(), taskReq.getComment());

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(taskReq.getTaskId())
                .withVariables(taskReq.getVariables())
                .build());

//        if(variables.containsValue(Boolean.FALSE)){
//            final String businessKey = task.getBusinessKey();
//            UpdateWrapper<OaLeaveDO> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.eq("id", Long.valueOf(businessKey));
//            OaLeaveDO updateDo = new OaLeaveDO();
//            updateDo.setStatus(2);
//            leaveMapper.update(updateDo, updateWrapper);
//        }

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

//        String processKey = taskQuery.getProcessKey();
//        if ("leave".equals(processKey)) {
//            String businessKey = taskQuery.getBusinessKey();
//            final OaLeaveDO leave = leaveMapper.selectById(Long.valueOf(businessKey));
//            handleVO.setFormObject( OaLeaveConvert.INSTANCE.convert(leave));
//        }

//
//        final String taskDefKey = task.getTaskDefinitionKey();
//        final String variableName = Optional.ofNullable(taskVariable.get(taskDefKey)).orElse("");
//        handleVO.setTaskVariable(variableName);
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
            // TODO @jason：放到 convert 里
            TaskStepVO step = new TaskStepVO();
            step.setStepName(instance.getActivityName());
            step.setStartTime(instance.getStartTime());
            step.setEndTime(instance.getEndTime());
            step.setAssignee(instance.getAssignee());
            step.setStatus(1);
            // TODO @jason：一般判数组为空，使用 CollUtil.isEmpty 会好点哈。另外，null 时候，不用填写 "" 的哈
            List<Comment> comments = activitiTaskService.getTaskComments(instance.getTaskId());
            if (comments.size() > 0) {
                step.setComment(comments.get(0).getFullMessage());
            } else {
                step.setComment("");
            }
            steps.add(step);
        });

        // 获得未完成的活动
        List<HistoricActivityInstance> unfinished = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .unfinished().list();
        // 获得对应的步骤
        // TODO @json：其实已完成和未完成，它们的 convert 的逻辑，是一致的
        for (HistoricActivityInstance instance : unfinished) {
            TaskStepVO step = new TaskStepVO();
            step.setStepName(instance.getActivityName());
            step.setStartTime(instance.getStartTime());
            step.setEndTime(instance.getEndTime());
            step.setAssignee(Optional.ofNullable(instance.getAssignee()).orElse(""));
            step.setComment("");
            step.setStatus(0);
            steps.add(step);
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
