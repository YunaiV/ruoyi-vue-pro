package cn.iocoder.yudao.adminserver.modules.bpm.controller.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task.*;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Api(tags = "流程任务")
@RestController
@RequestMapping("/bpm/task")
@Validated
public class BpmTaskController {

    @Resource
    private BpmTaskService taskService;

    // TODO 芋艿：权限、validation；

    @GetMapping("todo-page")
    @ApiOperation("获取 Todo 待办任务分页")
    public CommonResult<PageResult<BpmTaskTodoPageItemRespVO>> getTodoTaskPage(@Valid BpmTaskTodoPageReqVO pageVO) {
        return success(taskService.getTodoTaskPage(getLoginUserId(), pageVO));
    }

    @GetMapping("done-page")
    @ApiOperation("获取 Done 已办任务分页")
    public CommonResult<PageResult<BpmTaskDonePageItemRespVO>> getTodoTaskPage(@Valid BpmTaskDonePageReqVO pageVO) {
        return success(taskService.getDoneTaskPage(getLoginUserId(), pageVO));
    }

    @PutMapping("/complete")
    @ApiOperation(value = "完成任务", notes = "审批通过 or 不通过")
    public CommonResult<Boolean> completeTask(@Valid @RequestBody BpmTaskCompleteReqVO reqVO) {
        taskService.completeTask(reqVO);
        return success(true);
    }

    @PostMapping("/task-steps")
    public CommonResult<TaskHandleVO> getTaskSteps(@RequestBody TaskQueryReqVO taskQuery) {
        return success(taskService.getTaskSteps(taskQuery));
    }

    @GetMapping("/process/history-steps")
    public CommonResult<List<TaskStepVO>> getHistorySteps(@RequestParam("id") String processInstanceId) {
        return success(taskService.getHistorySteps(processInstanceId));
    }

    /**
     * 返回高亮的流转图SVG
     * @param processInstanceId 流程Id
     */
    @GetMapping("/process/highlight-img")
    public void getHighlightImg(@RequestParam String processInstanceId, HttpServletResponse response) throws IOException {
        FileResp fileResp = taskService.getHighlightImg(processInstanceId);
        ServletUtils.writeAttachment(response, fileResp.getFileName(), fileResp.getFileByte());
    }

}
