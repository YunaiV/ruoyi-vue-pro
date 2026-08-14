package cn.iocoder.yudao.module.fms.controller.admin.closing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingOverviewRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 结账期间")
@RestController
@RequestMapping("/fms/closing/period")
@Validated
public class FmsClosingPeriodController {

    @Resource
    private FmsClosingPeriodService closingPeriodService;

    @GetMapping("/current-month")
    @Operation(summary = "获得当前会计期间")
    public CommonResult<String> getCurrentMonth(@RequestParam("accountSetId") @NotNull Long accountSetId) {
        return success(closingPeriodService.getCurrentMonth(accountSetId, getLoginUserId()));
    }

    @GetMapping("/overview")
    @Operation(summary = "获得结账概况")
    @PreAuthorize("@ss.hasPermission('fms:closing:query')")
    public CommonResult<FmsClosingOverviewRespVO> getClosingOverview(@Valid FmsClosingQueryReqVO queryReqVO) {
        return success(closingPeriodService.getClosingOverview(queryReqVO, getLoginUserId()));
    }

    @PutMapping("/close")
    @Operation(summary = "结账")
    @PreAuthorize("@ss.hasPermission('fms:closing:close')")
    public CommonResult<Boolean> closePeriod(@Valid @RequestBody FmsClosingQueryReqVO queryReqVO) {
        closingPeriodService.closePeriod(queryReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/cancel")
    @Operation(summary = "反结账")
    @PreAuthorize("@ss.hasPermission('fms:closing:cancel')")
    public CommonResult<Boolean> cancelClosePeriod(@Valid FmsClosingQueryReqVO queryReqVO) {
        closingPeriodService.cancelClosePeriod(queryReqVO, getLoginUserId());
        return success(true);
    }

}
