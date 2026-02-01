package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotSubDeviceRegisterFullReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterRespDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 网关 CoAP 协议的【子设备动态注册】处理器
 * <p>
 * 用于子设备的动态注册，需要网关认证
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/register-devices">阿里云 - 动态注册子设备</a>
 */
@Slf4j
public class IotCoapRegisterSubHandler extends IotCoapAbstractHandler {

    private final IotDeviceCommonApi deviceApi;

    public IotCoapRegisterSubHandler() {
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected CommonResult<Object> handle0(CoapExchange exchange) {
        // 1.1 解析通用参数（从 URI 路径获取网关设备信息）
        List<String> uriPath = exchange.getRequestOptions().getUriPath();
        String productKey = getProductKey(uriPath);
        String deviceName = getDeviceName(uriPath);
        // 1.2 解析子设备列表
        SubDeviceRegisterRequest request = deserializeRequest(exchange, SubDeviceRegisterRequest.class);
        Assert.notNull(request, "请求参数不能为空");
        Assert.notEmpty(request.getParams(), "params 不能为空");

        // 2. 调用子设备动态注册
        IotSubDeviceRegisterFullReqDTO reqDTO = new IotSubDeviceRegisterFullReqDTO()
                .setGatewayProductKey(productKey)
                .setGatewayDeviceName(deviceName)
                .setSubDevices(request.getParams());
        CommonResult<List<IotSubDeviceRegisterRespDTO>> result = deviceApi.registerSubDevices(reqDTO);
        result.checkError();

        // 3. 返回结果
        return success(result.getData());
    }

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Override
    protected String getProductKey(List<String> uriPath) {
        // 路径格式：/auth/register/sub-device/{productKey}/{deviceName}
        return CollUtil.get(uriPath, 3);
    }

    @Override
    protected String getDeviceName(List<String> uriPath) {
        // 路径格式：/auth/register/sub-device/{productKey}/{deviceName}
        return CollUtil.get(uriPath, 4);
    }

    @Data
    public static class SubDeviceRegisterRequest {

        private List<IotSubDeviceRegisterReqDTO> params;

    }

}
