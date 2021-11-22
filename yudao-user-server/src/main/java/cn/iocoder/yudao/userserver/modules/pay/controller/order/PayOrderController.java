package cn.iocoder.yudao.userserver.modules.pay.controller.order;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayOrderCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitRespDTO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.userserver.modules.pay.controller.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.userserver.modules.pay.controller.order.vo.PayOrderSubmitRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.Map;

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

    @Resource
    private PayRefundCoreService payRefundCoreService;

    @PostMapping("/submit")
    @ApiOperation("提交支付订单")
//    @PreAuthenticated // TODO 暂时不加登陆验证，前端暂时没做好
    public CommonResult<PayOrderSubmitRespVO> submitPayOrder(@RequestBody PayOrderSubmitReqVO reqVO) {
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

    // ========== 支付渠道的回调 ==========

    @PostMapping("/notify/wx-pub/{channelId}")
    @ApiOperation("通知微信公众号支付的结果")
    public String notifyWxPayOrder(@PathVariable("channelId") Long channelId,
                                   @RequestBody String xmlData) throws Exception {
        payOrderCoreService.notifyPayOrder(channelId, PayChannelEnum.WX_PUB.getCode(), PayNotifyDataDTO.builder().body(xmlData).build());
        return "success";
    }

    @PostMapping("/notify/alipay-qr/{channelId}")
    @ApiOperation("通知支付宝扫码支付的结果")
    public String notifyAlipayQrPayOrder(@PathVariable("channelId") Long channelId,
                                         @RequestParam Map<String, String> params,
                                         @RequestBody String originData) throws Exception{
        payOrderCoreService.notifyPayOrder(channelId, PayChannelEnum.ALIPAY_QR.getCode(),
                PayNotifyDataDTO.builder().params(params).body(originData).build());
        return "success";
    }

    @GetMapping(value = "/return/alipay-qr/{channelId}")
    @ApiOperation("支付宝 wap 页面回跳")
    public String returnAliPayQrPayOrder(@PathVariable("channelId") Long channelId){
        //TODO @jason 校验 是否支付宝调用。 支付宝publickey 可以根据 appId 跳转不同的页面
        System.out.println("支付成功");
        return "支付成功";
    }

    @PostMapping(value = "/notify/alipay-wap/{channelId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation("支付宝 wap 页面回调")
    public String notifyAliPayWapPayOrder(@PathVariable("channelId") Long channelId,
                                          @RequestParam Map<String, String> params,
                                          @RequestBody String originData) throws Exception {
        //TODO 校验是否支付宝调用。 payclient 中加一个校验方法
        //支付宝退款交易也会触发支付回调接口
        //参考 https://opensupport.alipay.com/support/helpcenter/193/201602484851
        //判断是否为支付宝的退款交易
        if(isAliPayRefund(params)) {
            //退款通知
            payRefundCoreService.notifyPayRefund(channelId,PayChannelEnum.ALIPAY_WAP.getCode(), PayNotifyDataDTO.builder().params(params).body(originData).build());
        }else{
            //支付通知
            payOrderCoreService.notifyPayOrder(channelId, PayChannelEnum.ALIPAY_WAP.getCode(), PayNotifyDataDTO.builder().params(params).body(originData).build());
        }
        return "success";
    }


    /**
     * https://opendocs.alipay.com/open/203/105285#%E5%89%8D%E5%8F%B0%E5%9B%9E%E8%B7%B3%E5%8F%82%E6%95%B0%E8%AF%B4%E6%98%8E
     * @param channelId 渠道id
     * @return 返回跳转页面
     */
    @GetMapping(value = "/return/alipay-wap/{channelId}")
    @ApiOperation("支付宝 wap 页面回跳")
    public String returnAliPayWapPayOrder(@PathVariable("channelId") Long channelId){
        //TODO 校验 是否支付宝调用。 可以根据 appId 跳转不同的页面
        return "支付成功";
    }

    /**
     * 是否是支付宝的退款交易
     * @param params http content-type application/x-www-form-urlencoded 的参数
     * @return
     */
    private boolean  isAliPayRefund(Map<String, String> params) {
        if (params.containsKey("refund_fee")) {
            return true;
        } else {
            return false;
        }
    }

    @RequestMapping("/notify/test")
    @ApiOperation("通知的测试接口")
    public String notifyTest() {
//        System.out.println(data);
        return "success";
    }

}
