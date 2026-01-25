package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.ext.web.RoutingContext;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 网关 HTTP 协议的【子设备动态注册】处理器
 * <p>
 * 用于子设备的动态注册，需要网关认证
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/register-devices">阿里云 - 动态注册子设备</a>
 */
public class IotHttpRegisterSubHandler extends IotHttpAbstractHandler {

    /**
     * 路径：/auth/register/sub-device/:productKey/:deviceName
     * <p>
     * productKey 和 deviceName 是网关设备的标识
     */
    public static final String PATH = "/auth/register/sub-device/:productKey/:deviceName";

    private final IotHttpUpstreamProtocol protocol;

    private final IotDeviceMessageService deviceMessageService;

    public IotHttpRegisterSubHandler(IotHttpUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    @Override
    public CommonResult<Object> handle0(RoutingContext context) {
        // 1. 解析通用参数
        String productKey = context.pathParam("productKey");
        String deviceName = context.pathParam("deviceName");

        // 2.1 解析消息
        byte[] bytes = context.body().buffer().getBytes();
        IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(bytes, productKey, deviceName);
        // 2.2 设置方法
        message.setMethod(IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod());

        // TODO @AI：可能还是需要一个新的 deviceApi 接口。因为 register sub 子设备不太一行；
        // 2.3 发送消息
        Object responseData = deviceMessageService.sendDeviceMessage(message, productKey, deviceName, protocol.getServerId());

        // 3. 返回结果
        return success(responseData);
    }

}
