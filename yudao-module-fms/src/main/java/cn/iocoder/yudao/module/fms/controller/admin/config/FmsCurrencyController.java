package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.currency.FmsCurrencyRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.currency.FmsCurrencySaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.service.config.FmsCurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 币别")
@RestController
@RequestMapping("/fms/config/currency")
@Validated
public class FmsCurrencyController {

    @Resource
    private FmsCurrencyService currencyService;

    @PostMapping("/create")
    @Operation(summary = "创建币别")
    @PreAuthorize("@ss.hasPermission('fms:config:currency:create')")
    public CommonResult<Long> createCurrency(@Valid @RequestBody FmsCurrencySaveReqVO createReqVO) {
        return success(currencyService.createCurrency(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新币别")
    @PreAuthorize("@ss.hasPermission('fms:config:currency:update')")
    public CommonResult<Boolean> updateCurrency(@Valid @RequestBody FmsCurrencySaveReqVO updateReqVO) {
        currencyService.updateCurrency(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除币别")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "id", description = "币别编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('fms:config:currency:delete')")
    public CommonResult<Boolean> deleteCurrency(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("id") @NotNull Long id) {
        currencyService.deleteCurrency(accountSetId, id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得币别列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:currency:query')")
    public CommonResult<List<FmsCurrencyRespVO>> getCurrencyList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        List<FmsCurrencyDO> list = currencyService.getCurrencyList(accountSetId, getLoginUserId());
        return success(BeanUtils.toBean(list, FmsCurrencyRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得币别精简列表", description = "主要用于前端的下拉选项")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    public CommonResult<List<FmsCurrencyRespVO>> getCurrencySimpleList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        List<FmsCurrencyDO> list = currencyService.getCurrencyList(accountSetId, getLoginUserId());
        return success(convertList(list, currency -> new FmsCurrencyRespVO()
                .setId(currency.getId()).setCode(currency.getCode()).setName(currency.getName())
                .setExchangeRate(currency.getExchangeRate()).setStandard(currency.getStandard())));
    }

}
