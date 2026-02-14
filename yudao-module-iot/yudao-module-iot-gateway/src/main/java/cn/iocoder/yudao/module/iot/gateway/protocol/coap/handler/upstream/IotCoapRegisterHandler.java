package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * IoT 网关 CoAP 协议的【设备动态注册】处理器
 * <p>
 * 用于直连设备/网关的一型一密动态注册，不需要认证
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
 */
@Slf4j
public class IotCoapRegisterHandler extends IotCoapAbstractHandler {

    private final IotDeviceCommonApi deviceApi;

    public IotCoapRegisterHandler() {
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    @Override
    protected CommonResult<Object> handle0(CoapExchange exchange) {
        // 1. 解析参数
        IotDeviceRegisterReqDTO request = deserializeRequest(exchange, IotDeviceRegisterReqDTO.class);
        Assert.notNull(request, "请求体不能为空");
        Assert.notBlank(request.getProductKey(), "productKey 不能为空");
        Assert.notBlank(request.getDeviceName(), "deviceName 不能为空");
        Assert.notBlank(request.getSign(), "sign 不能为空");

        // 2. 调用动态注册
        CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(request);
        result.checkError();

        // 3. 构建响应数据
        return CommonResult.success(result.getData());
    }

}
