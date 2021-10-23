package cn.iocoder.yudao.userserver.modules.pay.controller.order;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayOrderCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitRespDTO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.userserver.modules.pay.controller.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.userserver.modules.pay.controller.order.vo.PayOrderSubmitRespVO;
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

@Api(tags = "支付订单")
@RestController
@RequestMapping("/pay/order")
@Validated
@Slf4j
public class PayOrderController {

    @Resource
    private PayOrderCoreService payOrderCoreService;

    @PostMapping("/submit")
    @ApiOperation("提交支付订单")
//    @PreAuthenticated // TODO 暂时不加登陆验证，前端暂时没做好
    public CommonResult<PayOrderSubmitRespVO> submit(@RequestBody PayOrderSubmitReqVO reqVO) {
        // 获得订单
        PayOrderDO payOrder = payOrderCoreService.getPayOrder(reqVO.getId());

        // 提交支付
        PayOrderSubmitReqDTO reqDTO = new PayOrderSubmitReqDTO();
        BeanUtil.copyProperties(reqVO, reqDTO, false);
        reqDTO.setUserIp(getClientIP());
        reqDTO.setAppId(payOrder.getAppId());
        PayOrderSubmitRespDTO respDTO = payOrderCoreService.submitPayOrder(reqDTO);

        // 拼接返回
        return success(PayOrderSubmitRespVO.builder().invokeResponse(respDTO.getInvokeResponse()).build());
    }

}
