package cn.iocoder.yudao.module.bpm.controller.admin.task;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程任务实例")
@RestController
@RequestMapping("/bpm/task")
@Validated
public class BpmTaskController {

    @Resource
    private BpmTaskService taskService;

    @GetMapping("todo-page")
    @Operation(summary = "获取 Todo 待办任务分页")
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<PageResult<BpmTaskTodoPageItemRespVO>> getTodoTaskPage(@Valid BpmTaskTodoPageReqVO pageVO) {
        return success(taskService.getTodoTaskPage(getLoginUserId(), pageVO));
    }

    @GetMapping("done-page")
    @Operation(summary = "获取 Done 已办任务分页")
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<PageResult<BpmTaskDonePageItemRespVO>> getDoneTaskPage(@Valid BpmTaskDonePageReqVO pageVO) {
        return success(taskService.getDoneTaskPage(getLoginUserId(), pageVO));
    }

    @GetMapping("/list-by-process-instance-id")
    @Operation(summary = "获得指定流程实例的任务列表", description = "包括完成的、未完成的")
    @Parameter(name = "processInstanceId", description = "流程实例的编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<List<BpmTaskRespVO>> getTaskListByProcessInstanceId(
            @RequestParam("processInstanceId") String processInstanceId) {
        return success(taskService.getTaskListByProcessInstanceId(processInstanceId));
    }

    @PutMapping("/approve")
    @Operation(summary = "通过任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> approveTask(@Valid @RequestBody BpmTaskApproveReqVO reqVO) {
        taskService.approveTask(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "不通过任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> rejectTask(@Valid @RequestBody BpmTaskRejectReqVO reqVO) {
        taskService.rejectTask(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/update-assignee")
    @Operation(summary = "更新任务的负责人", description = "用于【流程详情】的【转派】按钮")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> updateTaskAssignee(@Valid @RequestBody BpmTaskUpdateAssigneeReqVO reqVO) {
        taskService.updateTaskAssignee(getLoginUserId(), reqVO);
        return success(true);
    }

    @GetMapping("/get-return-list")
    @Operation(summary = "获取所有可回退的节点", description = "用于【流程详情】的【回退】按钮")
    @Parameter(name = "taskId", description = "当前任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<List<BpmTaskSimpleRespVO>> getReturnList(@RequestParam("taskId") String taskId) {
        return success(taskService.getReturnTaskList(taskId));
    }

    @PutMapping("/return")
    @Operation(summary = "回退任务", description = "用于【流程详情】的【回退】按钮")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> returnTask(@Valid @RequestBody BpmTaskReturnReqVO reqVO) {
        taskService.returnTask(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/delegate")
    @Operation(summary = "委派任务", description = "用于【流程详情】的【委派】按钮。和向前【加签】有点像，唯一区别是【委托】没有单独创立任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> delegateTask(@Valid @RequestBody BpmTaskDelegateReqVO reqVO) {
        // TODO @海：, 后面要有空格
        taskService.delegateTask(reqVO,getLoginUserId());
        return success(true);
    }

    // TODO @海：权限统一使用 bpm:task:update；是否可以加减签，可以交给后续的权限配置实现；
    @PutMapping("/add-sign")
    @Operation(summary = "加签", description = "before 前加签，after 后加签")
    @PreAuthorize("@ss.hasPermission('bpm:task:add-sign')")
    public CommonResult<Boolean> addSign(@Valid @RequestBody BpmTaskAddSignReqVO reqVO) {
        // TODO @海：userId 建议作为第一个参数；一般是，谁做了什么操作；另外，addSignTask，保持风格统一哈；
        taskService.addSign(reqVO,getLoginUserId());
        return success(true);
    }

    // TODO @海：权限统一使用 bpm:task:update；是否可以加减签，可以交给后续的权限配置实现；
    @PutMapping("/sub-sign")
    @Operation(summary = "减签")
    @PreAuthorize("@ss.hasPermission('bpm:task:sub-sign')")
    // TODO @海： @RequestBody  BpmTaskSubSignReqVO 应该是一个空格；然后参数名可以简写成 reqVO；
    public CommonResult<Boolean> subSign(@Valid @RequestBody  BpmTaskSubSignReqVO bpmTaskSubSignReqVO) {
        taskService.subSign(bpmTaskSubSignReqVO,getLoginUserId());
        return success(true);
    }

    // TODO @海：是不是 url 和方法名，叫 get-child-task-list，更抽象和复用一些？
    @GetMapping("/get-sub-sign")
    @Operation(summary = "获取能被减签的任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:sub-sign')")
    public CommonResult<List<BpmTaskSubSignRespVO>> getChildrenTaskList(@RequestParam("taskId") String taskId) {
        return success(taskService.getChildrenTaskList(taskId));
    }

}
