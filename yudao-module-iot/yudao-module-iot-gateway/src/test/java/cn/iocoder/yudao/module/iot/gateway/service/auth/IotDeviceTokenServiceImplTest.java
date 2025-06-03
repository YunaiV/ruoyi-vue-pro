package cn.iocoder.yudao.module.iot.gateway.service.auth;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link IotDeviceTokenServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class IotDeviceTokenServiceImplTest {

    @Mock
    private IotGatewayProperties gatewayProperties;

    @InjectMocks
    private IotDeviceTokenServiceImpl tokenService;

    private IotGatewayProperties.TokenProperties tokenProperties;

    @BeforeEach
    void setUp() {
        // 初始化 Token 配置
        tokenProperties = new IotGatewayProperties.TokenProperties();
        tokenProperties.setSecret("1234567890123456789012345678901");
        tokenProperties.setExpiration(Duration.ofDays(7));
        
        when(gatewayProperties.getToken()).thenReturn(tokenProperties);
    }

    @Test
    void testCreateToken_Success() {
        // 准备参数
        String productKey = "testProduct";
        String deviceName = "testDevice";

        // 调用方法
        String token = tokenService.createToken(productKey, deviceName);

        // 验证结果
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testCreateToken_WithBlankParameters() {
        // 测试空白参数
        assertNull(tokenService.createToken("", "deviceName"));
        assertNull(tokenService.createToken("productKey", ""));
        assertNull(tokenService.createToken(null, "deviceName"));
        assertNull(tokenService.createToken("productKey", null));
    }

    @Test
    void testCreateToken_WithoutConfig() {
        // 模拟配置为空
        when(gatewayProperties.getToken()).thenReturn(null);

        // 调用方法
        String token = tokenService.createToken("productKey", "deviceName");

        // 验证结果
        assertNull(token);
    }

    @Test
    void testVerifyToken_Success() {
        // 准备参数
        String productKey = "testProduct";
        String deviceName = "testDevice";

        // 创建 Token
        String token = tokenService.createToken(productKey, deviceName);
        assertNotNull(token);

        // 验证 Token
        IotDeviceAuthUtils.DeviceInfo deviceInfo = tokenService.verifyToken(token);

        // 验证结果
        assertNotNull(deviceInfo);
        assertEquals(productKey, deviceInfo.getProductKey());
        assertEquals(deviceName, deviceInfo.getDeviceName());
    }

    @Test
    void testVerifyToken_WithBlankToken() {
        // 测试空白 Token
        assertNull(tokenService.verifyToken(""));
        assertNull(tokenService.verifyToken(null));
    }

    @Test
    void testVerifyToken_WithInvalidToken() {
        // 测试无效 Token
        assertNull(tokenService.verifyToken("invalid.token.here"));
    }

    @Test
    void testVerifyToken_WithoutConfig() {
        // 模拟配置为空
        when(gatewayProperties.getToken()).thenReturn(null);

        // 调用方法
        IotDeviceAuthUtils.DeviceInfo deviceInfo = tokenService.verifyToken("any.token.here");

        // 验证结果
        assertNull(deviceInfo);
    }

    @Test
    void testTokenRoundTrip() {
        // 测试完整的 Token 创建和验证流程
        String productKey = "myProduct";
        String deviceName = "myDevice";

        // 1. 创建 Token
        String token = tokenService.createToken(productKey, deviceName);
        assertNotNull(token);

        // 2. 验证 Token
        IotDeviceAuthUtils.DeviceInfo deviceInfo = tokenService.verifyToken(token);
        assertNotNull(deviceInfo);
        assertEquals(productKey, deviceInfo.getProductKey());
        assertEquals(deviceName, deviceInfo.getDeviceName());
    }

} 