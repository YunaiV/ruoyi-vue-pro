package cn.iocoder.yudao.module.iot.gateway.service.auth;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_TOKEN_EXPIRED;

/**
 * IoT 设备 Token Service 实现类：调用远程的 device http 接口，进行设备 Token 生成、解析等逻辑
 *
 * 注意：目前仅 HTTP 协议使用
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceTokenServiceImpl implements IotDeviceTokenService {

    @Resource
    private IotGatewayProperties gatewayProperties;

    @Override
    public String createToken(String productKey, String deviceName) {
        Assert.notBlank(productKey, "productKey 不能为空");
        Assert.notBlank(deviceName, "deviceName 不能为空");
        // 构建 JWT payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("productKey", productKey);
        payload.put("deviceName", deviceName);
        LocalDateTime expireTime = LocalDateTimeUtils.addTime(gatewayProperties.getToken().getExpiration());
        payload.put("exp", LocalDateTimeUtils.toEpochSecond(expireTime)); // 过期时间（exp 是 JWT 规范推荐）

        // 生成 JWT Token
        return JWTUtil.createToken(payload, gatewayProperties.getToken().getSecret().getBytes());
    }

    @Override
    public IotDeviceAuthUtils.DeviceInfo verifyToken(String token) {
        Assert.notBlank(token, "token 不能为空");
        // 校验 JWT Token
        boolean verify = JWTUtil.verify(token, gatewayProperties.getToken().getSecret().getBytes());
        if (!verify) {
            throw exception(DEVICE_TOKEN_EXPIRED);
        }

        // 解析 Token
        JWT jwt = JWTUtil.parseToken(token);
        JSONObject payload = jwt.getPayloads();
        // 检查过期时间
        Long exp = payload.getLong("exp");
        if (exp == null || exp < System.currentTimeMillis() / 1000) {
            throw exception(DEVICE_TOKEN_EXPIRED);
        }
        String productKey = payload.getStr("productKey");
        String deviceName = payload.getStr("deviceName");
        Assert.notBlank(productKey, "productKey 不能为空");
        Assert.notBlank(deviceName, "deviceName 不能为空");
        return new IotDeviceAuthUtils.DeviceInfo().setProductKey(productKey).setDeviceName(deviceName);
    }

    @Override
    public IotDeviceAuthUtils.DeviceInfo parseUsername(String username) {
        return IotDeviceAuthUtils.parseUsername(username);
    }

}
