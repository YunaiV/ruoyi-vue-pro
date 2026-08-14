package cn.iocoder.yudao.module.fms.controller.admin.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeMetricDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeRespVO;
import cn.iocoder.yudao.module.fms.service.home.FmsHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 首页")
@RestController
@RequestMapping("/fms/home")
@Validated
public class FmsHomeController {

    @Resource
    private FmsHomeService homeService;

    @GetMapping("/get")
    @Operation(summary = "获得首页")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:home:query')")
    public CommonResult<FmsHomeRespVO> getHome(@RequestParam("accountSetId") @NotNull Long accountSetId) {
        return success(homeService.getHome(accountSetId, getLoginUserId()));
    }

    @GetMapping("/metric-detail")
    @Operation(summary = "获得财务指标明细")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "metricKey", description = "财务指标标识", required = true, example = "income")
    })
    @PreAuthorize("@ss.hasPermission('fms:home:query')")
    public CommonResult<FmsHomeMetricDetailRespVO> getMetricDetail(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("metricKey") @NotBlank String metricKey) {
        return success(homeService.getMetricDetail(accountSetId, metricKey, getLoginUserId()));
    }

}
