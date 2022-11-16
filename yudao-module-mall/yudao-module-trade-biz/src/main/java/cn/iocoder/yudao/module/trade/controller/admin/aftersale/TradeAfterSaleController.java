package cn.iocoder.yudao.module.trade.controller.admin.aftersale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleAuditReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleConfirmReqVO;
import cn.iocoder.yudao.module.trade.service.aftersale.TradeAfterSaleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "管理后台 - 交易售后")
@RestController
@RequestMapping("/trade/after-sale")
@Validated
@Slf4j
public class TradeAfterSaleController {

    @Resource
    private TradeAfterSaleService afterSaleService;

    @PutMapping("/audit")
    @ApiOperation("审批售后")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:audit')")
    public CommonResult<Boolean> auditAfterSale(@RequestBody TradeAfterSaleAuditReqVO auditReqVO) {
        afterSaleService.auditAfterSale(getLoginUserId(), getClientIP(), auditReqVO);
        return success(true);
    }

    @PutMapping("/confirm")
    @ApiOperation("确认收货")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:audit')")
    public CommonResult<Boolean> confirmAfterSale(@RequestBody TradeAfterSaleConfirmReqVO confirmReqVO) {
        afterSaleService.confirmAfterSale(getLoginUserId(), getClientIP(), confirmReqVO);
        return success(true);
    }

    @PostMapping("/refund")
    @ApiOperation(value = "确认退款", notes = "提供给【pay】支付服务，退款成功后进行回调")
    @ApiImplicitParam(name = "payRefundId", value = "支付退款编号", required = true, example = "18888")
    @PermitAll
    public CommonResult<Boolean> refundAfterSale(@RequestParam("payRefundId") Long payRefundId) {
        afterSaleService.refundAfterSale(payRefundId);
        return success(true);
    }

}
