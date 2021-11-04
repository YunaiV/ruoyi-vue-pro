package cn.iocoder.yudao.userserver.modules.pay.controller.order;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayOrderCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitRespDTO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.NotifyDataDTO;
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
        payOrderCoreService.notifyPayOrder(channelId, PayChannelEnum.WX_PUB.getCode(), NotifyDataDTO.builder().origData(xmlData).build());
        return "success";
    }

    @PostMapping("/notify/alipay-qr/{channelId}")
    @ApiOperation("通知支付宝扫码支付的结果")
    public String notifyAlipayQrPayOrder(@PathVariable("channelId") Long channelId,
                                         @RequestBody String data) {
        return "success";
    }

    @RequestMapping("/notify/test")
    @ApiOperation("通知的测试接口")
    public String notifyTest() {
//        System.out.println(data);
        return "success";
    }

    @PostMapping(value = "/notify/alipay-wap/{channelId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation("支付宝wap页面回调")
    public String notifyAliPayWapPayOrder(@PathVariable("channelId") Long channelId,
                                          @RequestParam Map<String, String> params,
                                          @RequestBody String originData) throws Exception {
        //TODO @jason 校验 是否支付宝调用。 使用 支付宝publickey payclient 或许加一个校验方法
        payOrderCoreService.notifyPayOrder(channelId, PayChannelEnum.ALIPAY_WAP.getCode(), NotifyDataDTO.builder().params(params).origData(originData).build());
        return "success";
    }

    // TODO @jason 如果有些字段不注释，可以删除哈。不然 IDEA 会报警
    /**
     * https://opendocs.alipay.com/open/203/105285#%E5%89%8D%E5%8F%B0%E5%9B%9E%E8%B7%B3%E5%8F%82%E6%95%B0%E8%AF%B4%E6%98%8E
     * @param channelId
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/return/alipay-wap/{channelId}")
    @ApiOperation("支付宝wap页面回跳")
    public String returnAliPayWapPayOrder(@PathVariable("channelId") Long channelId){
        //TODO @jason 校验 是否支付宝调用。 支付宝publickey 可以根据 appId 跳转不同的页面
        return "支付成功";
    }

}
