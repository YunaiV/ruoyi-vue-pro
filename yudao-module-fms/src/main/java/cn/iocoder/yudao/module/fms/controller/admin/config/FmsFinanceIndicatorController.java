package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeindicator.FmsFinanceIndicatorRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeindicator.FmsFinanceIndicatorSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceIndicatorDO;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 财务指标")
@RestController
@RequestMapping("/fms/config/finance-indicator")
@Validated
public class FmsFinanceIndicatorController {

    @Resource
    private FmsFinanceIndicatorService financeIndicatorService;

    @PostMapping("/create")
    @Operation(summary = "创建首页财务指标")
    @PreAuthorize("@ss.hasPermission('fms:config:finance-indicator:create')")
    public CommonResult<Long> create(@Valid @RequestBody FmsFinanceIndicatorSaveReqVO reqVO) {
        return success(financeIndicatorService.createFinanceIndicator(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改首页财务指标")
    @PreAuthorize("@ss.hasPermission('fms:config:finance-indicator:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody FmsFinanceIndicatorSaveReqVO reqVO) {
        financeIndicatorService.updateFinanceIndicator(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除首页财务指标")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @Parameter(name = "id", description = "指标编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:finance-indicator:delete')")
    public CommonResult<Boolean> delete(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                        @RequestParam("id") @NotNull Long id) {
        financeIndicatorService.deleteFinanceIndicator(accountSetId, id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得首页财务指标")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @Parameter(name = "id", description = "指标编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:finance-indicator:query')")
    public CommonResult<FmsFinanceIndicatorRespVO> get(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                                       @RequestParam("id") @NotNull Long id) {
        FmsFinanceIndicatorDO indicator = financeIndicatorService.getFinanceIndicator(accountSetId, id, getLoginUserId());
        return success(BeanUtils.toBean(indicator, FmsFinanceIndicatorRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得首页财务指标列表")
    @PreAuthorize("@ss.hasPermission('fms:config:finance-indicator:query')")
    public CommonResult<List<FmsFinanceIndicatorRespVO>> getList(@RequestParam("accountSetId") @NotNull Long accountSetId) {
        List<FmsFinanceIndicatorDO> list = financeIndicatorService.getFinanceIndicatorList(accountSetId,
                getLoginUserId());
        return success(BeanUtils.toBean(list, FmsFinanceIndicatorRespVO.class));
    }

}
