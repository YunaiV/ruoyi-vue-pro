package cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.ext.web.RoutingContext;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_AUTH_FAIL;

/**
 * IoT 网关 HTTP 协议的【认证】处理器
 *
 * 参考 <a href="阿里云 IoT —— HTTPS 连接通信">https://help.aliyun.com/zh/iot/user-guide/establish-connections-over-https</a>
 *
 * @author 芋道源码
 */
public class IotHttpAuthHandler extends IotHttpAbstractHandler {

    public static final String PATH = "/auth";

    private final String serverId;

    private final IotDeviceTokenService deviceTokenService;

    private final IotDeviceCommonApi deviceApi;

    private final IotDeviceMessageService deviceMessageService;

    public IotHttpAuthHandler(IotHttpProtocol protocol) {
        this.serverId = protocol.getServerId();
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public CommonResult<Object> handle0(RoutingContext context) {
        // 1. 解析参数
        IotDeviceAuthReqDTO request = deserializeRequest(context, IotDeviceAuthReqDTO.class);
        Assert.notNull(request, "请求参数不能为空");
        Assert.notBlank(request.getClientId(), "clientId 不能为空");
        Assert.notBlank(request.getUsername(), "username 不能为空");
        Assert.notBlank(request.getPassword(), "password 不能为空");

        // 2.1 执行认证
        CommonResult<Boolean> result = deviceApi.authDevice(request);
        result.checkError();
        if (BooleanUtil.isFalse(result.getData())) {
            throw exception(DEVICE_AUTH_FAIL);
        }
        // 2.2 生成 Token
        IotDeviceIdentity deviceInfo = deviceTokenService.parseUsername(request.getUsername());
        Assert.notNull(deviceInfo, "设备信息不能为空");
        String token = deviceTokenService.createToken(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        Assert.notBlank(token, "生成 token 不能为空位");

        // 3. 执行上线
        IotDeviceMessage message = IotDeviceMessage.buildStateUpdateOnline();
        deviceMessageService.sendDeviceMessage(message,
                deviceInfo.getProductKey(), deviceInfo.getDeviceName(), serverId);

        // 构建响应数据
        return success(MapUtil.of("token", token));
    }

}
