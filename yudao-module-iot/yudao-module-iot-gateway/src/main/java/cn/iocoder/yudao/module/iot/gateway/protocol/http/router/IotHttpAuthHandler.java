package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
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

    private final IotHttpUpstreamProtocol protocol;

    private final IotDeviceTokenService deviceTokenService;

    private final IotDeviceCommonApi deviceApi;

    private final IotDeviceMessageService deviceMessageService;

    public IotHttpAuthHandler(IotHttpUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    @Override
    public CommonResult<Object> handle0(RoutingContext context) {
        // 1. 解析参数
        JsonObject body = context.body().asJsonObject();
        String clientId = body.getString("clientId");
        if (StrUtil.isEmpty(clientId)) {
            throw invalidParamException("clientId 不能为空");
        }
        String username = body.getString("username");
        if (StrUtil.isEmpty(username)) {
            throw invalidParamException("username 不能为空");
        }
        String password = body.getString("password");
        if (StrUtil.isEmpty(password)) {
            throw invalidParamException("password 不能为空");
        }

        // 2.1 执行认证
        CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                .setClientId(clientId).setUsername(username).setPassword(password));
        result.checkError();
        if (!BooleanUtil.isTrue(result.getData())) {
            throw exception(DEVICE_AUTH_FAIL);
        }
        // 2.2 生成 Token
        IotDeviceAuthUtils.DeviceInfo deviceInfo = deviceTokenService.parseUsername(username);
        Assert.notNull(deviceInfo, "设备信息不能为空");
        String token = deviceTokenService.createToken(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        Assert.notBlank(token, "生成 token 不能为空位");

        // 3. 执行上线
        IotDeviceMessage message = IotDeviceMessage.buildStateUpdateOnline();
        deviceMessageService.sendDeviceMessage(message,
                deviceInfo.getProductKey(), deviceInfo.getDeviceName(), protocol.getServerId());

        // 构建响应数据
        return success(MapUtil.of("token", token));
    }

}
