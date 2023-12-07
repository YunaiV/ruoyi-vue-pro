package cn.iocoder.yudao.module.pay.controller.admin.transfer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.*;
import cn.iocoder.yudao.module.pay.convert.transfer.PayTransferConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.service.transfer.PayTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;

@Tag(name = "管理后台 - 转账单")
@RestController
@RequestMapping("/pay/transfer")
@Validated
public class PayTransferController {

    @Resource
    private PayTransferService payTransferService;

    @PostMapping("/create")
    @Operation(summary = "创建转账单，发起转账")
    @PreAuthorize("@ss.hasPermission('pay:transfer:create')")
    public CommonResult<PayTransferCreateRespVO> createPayTransfer(@Valid @RequestBody PayTransferCreateReqVO reqVO) {
        PayTransferDO payTransfer = payTransferService.createTransfer(reqVO, getClientIP());
        return success(new PayTransferCreateRespVO().setId(payTransfer.getId()).setStatus(payTransfer.getStatus()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得转账订单")
    @PreAuthorize("@ss.hasPermission('pay:transfer:query')")
    public CommonResult<PayTransferRespVO> getTransfer(@RequestParam("id") Long id) {
        return success(PayTransferConvert.INSTANCE.convert(payTransferService.getTransfer(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得转账订单分页")
    @PreAuthorize("@ss.hasPermission('pay:transfer:query')")
    public CommonResult<PageResult<PayTransferPageItemRespVO>> getTransferPage(@Valid PayTransferPageReqVO pageVO) {
        PageResult<PayTransferDO> pageResult = payTransferService.getTransferPage(pageVO);
        return success(PayTransferConvert.INSTANCE.convertPage(pageResult));
    }
}
