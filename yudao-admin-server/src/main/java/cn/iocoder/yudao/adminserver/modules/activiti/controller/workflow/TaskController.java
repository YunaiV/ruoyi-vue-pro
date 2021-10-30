package cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo.*;
import cn.iocoder.yudao.adminserver.modules.activiti.service.workflow.TaskService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @json：swagger 和 validation 的注解，后续要补全下哈。可以等 workflow 基本写的差不多之后
@Api(tags = "工作流待办任务")
@RestController
@RequestMapping("/workflow/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @GetMapping("/todo/page")
    @ApiOperation("获取待办任务分页")
    public CommonResult<PageResult<TodoTaskRespVO>> getTodoTaskPage(@Valid TodoTaskPageReqVO pageVO) {
        return success(taskService.getTodoTaskPage(pageVO));
    }

    @GetMapping("/claim")
    @ApiOperation("签收任务")
    public CommonResult<Boolean> claimTask(@RequestParam("id") String taskId) {
        taskService.claimTask(taskId);
        return success(true);
    }

    @PostMapping("/task-steps")
    public CommonResult<TaskHandleVO> getTaskSteps(@RequestBody TaskQueryReqVO taskQuery) {
        return success(taskService.getTaskSteps(taskQuery));
    }

    @PostMapping("/formKey")
    public CommonResult<TodoTaskRespVO> getTaskFormKey(@RequestBody TaskQueryReqVO taskQuery) {
        return success(taskService.getTaskFormKey(taskQuery));
    }

    @PostMapping("/complete")
    public CommonResult<Boolean> complete(@RequestBody TaskReqVO taskReq) {
        taskService.completeTask(taskReq);
        return success(true);
    }

    @GetMapping("/process/history-steps")
    public CommonResult<List<TaskStepVO>> getHistorySteps(@RequestParam("id") String processInstanceId) {
        return success(taskService.getHistorySteps(processInstanceId));
    }

}
