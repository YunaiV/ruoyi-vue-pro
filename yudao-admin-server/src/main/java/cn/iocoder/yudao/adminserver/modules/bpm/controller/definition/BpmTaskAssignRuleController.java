package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "任务分配规则")
@RestController
@RequestMapping("/bpm/task-assign-rule")
@Validated
public class BpmTaskAssignRuleController {

    @Resource
    private BpmTaskAssignRuleService taskAssignRuleService;

    @GetMapping("/list")
    public CommonResult<List<BpmTaskAssignRuleRespVO>> getTaskAssignRuleList(
            @RequestParam(value = "modelId", required = false) String modelId,
            @RequestParam(value = "processDefinitionId", required = false) String processDefinitionId) {
        return success(taskAssignRuleService.getTaskAssignRuleList(modelId, processDefinitionId));
    }

    @PostMapping("/create")
    public CommonResult<Long> createTaskAssignRule(@Valid @RequestBody BpmTaskAssignRuleCreateReqVO reqVO) {
        return success(taskAssignRuleService.createTaskAssignRule(reqVO));
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateTaskAssignRule(@Valid @RequestBody BpmTaskAssignRuleUpdateReqVO reqVO) {
        taskAssignRuleService.updateTaskAssignRule(reqVO);
        return success(true);
    }

}
