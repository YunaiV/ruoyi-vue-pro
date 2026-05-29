package cn.iocoder.yudao.module.yaya.controller.app.pay;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderRespVO;
import cn.iocoder.yudao.module.yaya.service.pay.YayaMemberOrderService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/yaya/pay")
@Validated
public class YayaAppPayController {

    @Resource
    private YayaMemberOrderService memberOrderService;

    @PostMapping("/member-orders")
    public CommonResult<YayaAppMemberOrderRespVO> createMemberOrder(
            @Valid @RequestBody YayaAppMemberOrderCreateReqVO reqVO) {
        return success(memberOrderService.createMemberOrder(getLoginUserId(), reqVO, getClientIP()));
    }

    @PostMapping("/notify/order")
    @PermitAll
    public CommonResult<Boolean> notifyOrderPaid(@Valid @RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        memberOrderService.activateEntitlementByPayOrder(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }

}
