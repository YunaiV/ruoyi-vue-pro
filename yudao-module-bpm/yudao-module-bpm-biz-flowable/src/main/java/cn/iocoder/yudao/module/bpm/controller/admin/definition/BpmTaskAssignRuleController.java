package cn.iocoder.yudao.module.bpm.controller.admin.definition;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule.BpmTaskAssignRuleCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule.BpmTaskAssignRuleRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule.BpmTaskAssignRuleUpdateReqVO;
import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 任务分配规则")
@RestController
@RequestMapping("/bpm/task-assign-rule")
@Validated
public class BpmTaskAssignRuleController {

    @Resource
    private BpmTaskAssignRuleService taskAssignRuleService;

    @GetMapping("/list")
    @ApiOperation(value = "获得任务分配规则列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modelId", value = "模型编号", example = "1024", dataTypeClass = String.class),
            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义的编号", example = "2048", dataTypeClass = String.class)
    })
    @PreAuthorize("@ss.hasPermission('bpm:task-assign-rule:query')")
    public CommonResult<List<BpmTaskAssignRuleRespVO>> getTaskAssignRuleList(
            @RequestParam(value = "modelId", required = false) String modelId,
            @RequestParam(value = "processDefinitionId", required = false) String processDefinitionId) {
        return success(taskAssignRuleService.getTaskAssignRuleList(modelId, processDefinitionId));
    }

    @PostMapping("/create")
    @ApiOperation(value = "创建任务分配规则")
    @PreAuthorize("@ss.hasPermission('bpm:task-assign-rule:create')")
    public CommonResult<Long> createTaskAssignRule(@Valid @RequestBody BpmTaskAssignRuleCreateReqVO reqVO) {
        return success(taskAssignRuleService.createTaskAssignRule(reqVO));
    }

    @PutMapping("/update")
    @ApiOperation(value = "更新任务分配规则")
    @PreAuthorize("@ss.hasPermission('bpm:task-assign-rule:update')")
    public CommonResult<Boolean> updateTaskAssignRule(@Valid @RequestBody BpmTaskAssignRuleUpdateReqVO reqVO) {
        taskAssignRuleService.updateTaskAssignRule(reqVO);
        return success(true);
    }
}
