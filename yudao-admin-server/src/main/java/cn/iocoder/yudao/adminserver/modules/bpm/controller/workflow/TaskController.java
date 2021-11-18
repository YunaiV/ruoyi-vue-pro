package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.*;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmTaskService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @json：swagger 和 validation 的注解，后续要补全下哈。可以等 workflow 基本写的差不多之后
@Api(tags = "工作流待办任务")
@RestController
@RequestMapping("/workflow/task")
public class TaskController {

    @Resource
    private BpmTaskService bpmTaskService;

    @GetMapping("/todo/page")
    @ApiOperation("获取待办任务分页")
    public CommonResult<PageResult<TodoTaskRespVO>> getTodoTaskPage(@Valid TodoTaskPageReqVO pageVO) {
        return success(bpmTaskService.getTodoTaskPage(pageVO));
    }

    @GetMapping("/claim")
    @ApiOperation("签收任务")
    public CommonResult<Boolean> claimTask(@RequestParam("id") String taskId) {
        bpmTaskService.claimTask(taskId);
        return success(true);
    }

    @PostMapping("/task-steps")
    public CommonResult<TaskHandleVO> getTaskSteps(@RequestBody TaskQueryReqVO taskQuery) {
        return success(bpmTaskService.getTaskSteps(taskQuery));
    }

    @PostMapping("/formKey")
    public CommonResult<TodoTaskRespVO> getTaskFormKey(@RequestBody TaskQueryReqVO taskQuery) {
        return success(bpmTaskService.getTaskFormKey(taskQuery));
    }

    @PostMapping("/complete")
    public CommonResult<Boolean> complete(@RequestBody TaskReqVO taskReq) {
        bpmTaskService.completeTask(taskReq);
        return success(true);
    }

    @GetMapping("/process/history-steps")
    public CommonResult<List<TaskStepVO>> getHistorySteps(@RequestParam("id") String processInstanceId) {
        return success(bpmTaskService.getHistorySteps(processInstanceId));
    }

    /**
     * 返回高亮的流转图SVG
     * @param processInstanceId 流程Id
     */
    @GetMapping("/process/highlight-img")
    public void getHighlightImg(@RequestParam String processInstanceId, HttpServletResponse response) throws IOException {
        FileResp fileResp = bpmTaskService.getHighlightImg(processInstanceId);
        ServletUtils.writeAttachment(response, fileResp.getFileName(), fileResp.getFileByte());
    }

}
