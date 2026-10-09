package cn.iocoder.yudao.module.ai.controller.admin.model;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetLogRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetLogDO;
import cn.iocoder.yudao.module.ai.service.billing.AiBudgetLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 预算事件日志")
@RestController
@RequestMapping("/ai/budget-log")
@Validated
public class AiBudgetLogController {

    @Resource
    private AiBudgetLogService budgetLogService;

    @GetMapping("/page")
    @Operation(summary = "获得预算事件日志分页")
    @PreAuthorize("@ss.hasPermission('ai:budget-log:query')")
    public CommonResult<PageResult<AiBudgetLogRespVO>> getBudgetLogPage(@Valid AiBudgetLogPageReqVO pageReqVO) {
        PageResult<AiBudgetLogDO> pageResult = budgetLogService.getBudgetLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiBudgetLogRespVO.class));
    }

}
