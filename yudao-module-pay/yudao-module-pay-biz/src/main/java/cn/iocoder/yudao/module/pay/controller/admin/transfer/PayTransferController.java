package cn.iocoder.yudao.module.pay.controller.admin.transfer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferCreateRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.service.transfer.PayTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;

@Tag(name = "管理后台 - 转账单")
@RestController
@RequestMapping("/pay/transfer")
@Validated
public class PayTransferController {

    @Resource
    private PayTransferService payTransferService;

    @PostMapping("/initiate")
    @Operation(summary = "发起转账")
    // TODO @jason：权限的设置， 管理后台页面加的时候加一下
    public CommonResult<PayTransferCreateRespVO> initiatePayTransfer(@Valid @RequestBody PayTransferCreateReqVO reqVO) {
        PayTransferDO payTransfer = payTransferService.initiateTransfer(reqVO, getClientIP());
        return success(new PayTransferCreateRespVO().setId(payTransfer.getId()).setStatus(payTransfer.getStatus()));
    }

}
