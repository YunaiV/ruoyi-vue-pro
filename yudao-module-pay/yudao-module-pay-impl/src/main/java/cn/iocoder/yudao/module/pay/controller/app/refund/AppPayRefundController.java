package cn.iocoder.yudao.module.pay.controller.app.refund;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.controller.app.refund.vo.AppPayRefundReqVO;
import cn.iocoder.yudao.module.pay.controller.app.refund.vo.AppPayRefundRespVO;
import cn.iocoder.yudao.module.pay.convert.refund.PayRefundConvert;
import cn.iocoder.yudao.module.pay.service.order.dto.PayRefundReqDTO;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.module.pay.util.PaySeqUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;

@Api(tags = "用户 APP - 退款订单")
@RestController
@RequestMapping("/pay/refund")
@Validated
@Slf4j
public class AppPayRefundController {

    @Resource
    private PayRefundService refundService;

    @PostMapping("/refund")
    @ApiOperation("提交退款订单")
    public CommonResult<AppPayRefundRespVO> submitRefundOrder(@RequestBody AppPayRefundReqVO reqVO){
        PayRefundReqDTO req = PayRefundConvert.INSTANCE.convert(reqVO);
        req.setUserIp(getClientIP());
        // TODO 测试暂时模拟生成商户退款订单
        if(StrUtil.isEmpty(reqVO.getMerchantRefundId())) {
            req.setMerchantRefundId(PaySeqUtils.genMerchantRefundNo());
        }
        return success(PayRefundConvert.INSTANCE.convert(refundService.submitRefundOrder(req)));
    }

}
