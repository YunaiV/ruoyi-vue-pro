package cn.iocoder.yudao.module.fms.controller.admin.closing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingVoucherGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsProfitLossGenerateReqVO;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 结转凭证")
@RestController
@RequestMapping("/fms/closing/voucher")
@Validated
public class FmsClosingVoucherController {

    @Resource
    private FmsClosingVoucherService closingVoucherService;

    @PostMapping("/generate-profit-loss")
    @Operation(summary = "生成结转损益凭证")
    @PreAuthorize("@ss.hasPermission('fms:closing:profit-loss')")
    public CommonResult<Long> generateProfitLossVoucher(@Valid @RequestBody FmsProfitLossGenerateReqVO generateReqVO) {
        return success(closingVoucherService.generateProfitLossVoucher(generateReqVO, getLoginUserId()));
    }

    @PostMapping("/generate-scheme")
    @Operation(summary = "生成结账方案凭证")
    @PreAuthorize("@ss.hasPermission('fms:closing:profit-loss')")
    public CommonResult<Long> generateClosingSchemeVoucher(@Valid @RequestBody FmsClosingSchemeGenerateReqVO generateReqVO) {
        return success(closingVoucherService.generateClosingSchemeVoucher(generateReqVO, getLoginUserId()));
    }

    @PostMapping("/generate-list")
    @Operation(summary = "批量生成结转凭证")
    @PreAuthorize("@ss.hasPermission('fms:closing:profit-loss')")
    public CommonResult<List<Long>> generateClosingVoucherList(@Valid @RequestBody FmsClosingVoucherGenerateReqVO generateReqVO) {
        return success(closingVoucherService.generateClosingVoucherList(generateReqVO, getLoginUserId()));
    }

}
