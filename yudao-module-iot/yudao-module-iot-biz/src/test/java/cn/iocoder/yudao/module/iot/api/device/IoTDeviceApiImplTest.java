package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotGatewayServerAliveReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.aop.support.AopUtils;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link IoTDeviceApiImpl} 的单元测试：经过真实的 Spring 方法校验代理（与 @Validated 的 Bean 在容器中一致）
 *
 * @author 芋道源码
 */
public class IoTDeviceApiImplTest extends BaseMockitoUnitTest {

    private static final String KEEP_ALIVE_URL = "/rpc-api/iot/gateway-server/keep-alive";

    @InjectMocks
    private IoTDeviceApiImpl deviceApiImpl;

    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotProductService productService;
    @Mock
    private IotDeviceModbusConfigService modbusConfigService;
    @Mock
    private IotDeviceModbusPointService modbusPointService;
    @Mock
    private IotDevicePropertyService devicePropertyService;

    /**
     * 方法校验代理：参数约束继承不合法时，代理上的任何方法调用都会抛 ConstraintDeclarationException（HV000151）
     */
    private IotDeviceCommonApi deviceApi;

    @BeforeEach
    public void setUp() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(Validation.buildDefaultValidatorFactory().getValidator());
        processor.afterPropertiesSet();
        deviceApi = (IotDeviceCommonApi) processor.postProcessAfterInitialization(deviceApiImpl, "ioTDeviceApiImpl");
        assertTrue(AopUtils.isAopProxy(deviceApi));
    }

    @Test
    public void testAuthDevice() {
        // 准备参数
        IotDeviceAuthReqDTO reqDTO = new IotDeviceAuthReqDTO("client", "dn&pk", "password");
        // mock 方法
        when(deviceService.authDevice(same(reqDTO))).thenReturn(true);

        // 调用
        CommonResult<Boolean> result = deviceApi.authDevice(reqDTO);
        // 断言：方法校验代理正常放行
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
    }

    @Test
    public void testGetDevice_scriptEnabled() {
        // 准备参数
        IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO().setId(10L);
        // mock 方法：产品启用数据解析脚本
        when(deviceService.getDeviceFromCache(eq(10L))).thenReturn(IotDeviceDO.builder().id(10L).productId(1L)
                .productKey("pk").deviceName("dn").build());
        when(productService.getProductFromCache(eq(1L))).thenReturn(IotProductDO.builder().id(1L)
                .protocolType("tcp").serializeType("json")
                .scriptEnabled(true).scriptContent("function decode(ctx) {}").build());

        // 调用
        CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
        // 断言：产品字段随设备信息返回，启用时带上脚本内容
        assertTrue(result.isSuccess());
        IotDeviceRespDTO device = result.getData();
        assertEquals(10L, device.getId());
        assertEquals(1L, device.getProductId());
        assertEquals("tcp", device.getProtocolType());
        assertEquals("json", device.getSerializeType());
        assertTrue(device.getScriptEnabled());
        assertEquals("function decode(ctx) {}", device.getScriptContent());
    }

    @Test
    public void testGetDevice_scriptDisabled() {
        // 准备参数
        IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO().setProductKey("pk").setDeviceName("dn");
        // mock 方法：产品停用数据解析脚本，但保留了脚本内容
        when(deviceService.getDeviceFromCache(eq("pk"), eq("dn"))).thenReturn(IotDeviceDO.builder().id(10L)
                .productId(1L).productKey("pk").deviceName("dn").build());
        when(productService.getProductFromCache(eq(1L))).thenReturn(IotProductDO.builder().id(1L)
                .scriptEnabled(false).scriptContent("function decode(ctx) {}").build());

        // 调用
        CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
        // 断言：停用时不返回脚本内容
        assertTrue(result.isSuccess());
        assertFalse(result.getData().getScriptEnabled());
        assertNull(result.getData().getScriptContent());
    }

    @Test
    public void testKeepAliveGatewayServers_success() {
        // 准备参数
        Map<String, Long> startTimes = new LinkedHashMap<>();
        startTimes.put("server-1", 1000L);
        startTimes.put("server-2", 2000L);
        IotGatewayServerAliveReqDTO reqDTO = new IotGatewayServerAliveReqDTO().setStartTimes(startTimes);

        // 调用
        CommonResult<Boolean> result = deviceApi.keepAliveGatewayServers(reqDTO);
        // 断言：逐项写入网关上报的启动时间
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(devicePropertyService).updateGatewayServerStartTime(eq("server-1"), eq(1000L));
        verify(devicePropertyService).updateGatewayServerStartTime(eq("server-2"), eq(2000L));
    }

    @Test
    public void testKeepAliveGatewayServers_partialFail() {
        // 准备参数
        Map<String, Long> startTimes = new LinkedHashMap<>();
        startTimes.put("server-1", 1000L);
        startTimes.put("server-2", 2000L);
        startTimes.put("server-3", 3000L);
        IotGatewayServerAliveReqDTO reqDTO = new IotGatewayServerAliveReqDTO().setStartTimes(startTimes);
        // mock 方法：中间一项写入失败
        doThrow(new RedisConnectionFailureException("Redis 不可用")).when(devicePropertyService)
                .updateGatewayServerStartTime(eq("server-2"), eq(2000L));

        // 调用
        CommonResult<Boolean> result = deviceApi.keepAliveGatewayServers(reqDTO);
        // 断言：失败不影响后面的项，整体结果为 false
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
        verify(devicePropertyService).updateGatewayServerStartTime(eq("server-1"), eq(1000L));
        verify(devicePropertyService).updateGatewayServerStartTime(eq("server-3"), eq(3000L));
    }

    @Test
    public void testKeepAliveGatewayServers_httpInvalid() throws Exception {
        // 准备参数：非法的批次
        Map<String, String> invalidBodies = new LinkedHashMap<>();
        invalidBodies.put("startTimes 为 null", "{\"startTimes\":null}");
        invalidBodies.put("startTimes 为空", "{\"startTimes\":{}}");
        invalidBodies.put("serverId 为空白", "{\"startTimes\":{\" \":1000}}");
        invalidBodies.put("启动时间为 null", "{\"startTimes\":{\"server-1\":null}}");
        invalidBodies.put("启动时间为 0", "{\"startTimes\":{\"server-1\":0}}");
        invalidBodies.put("启动时间为负数", "{\"startTimes\":{\"server-1\":-1}}");
        invalidBodies.put("合法项与 null 混合", "{\"startTimes\":{\"server-1\":1000,\"server-2\":null}}");
        MockMvc mockMvc = buildMockMvc();

        for (Map.Entry<String, String> entry : invalidBodies.entrySet()) {
            // 调用
            int httpStatus = mockMvc.perform(post(KEEP_ALIVE_URL).contentType(MediaType.APPLICATION_JSON)
                    .content(entry.getValue())).andReturn().getResponse().getStatus();
            // 断言：被拒绝
            assertEquals(400, httpStatus, entry.getKey());
        }
        // 断言：任何一项都没有写入
        verifyNoInteractions(devicePropertyService);
    }

    @Test
    public void testKeepAliveGatewayServers_httpNoSizeLimit() throws Exception {
        // 准备参数：101 个协议实例
        StringBuilder body = new StringBuilder("{\"startTimes\":{");
        for (int i = 0; i < 101; i++) {
            body.append(i > 0 ? "," : "").append("\"server-").append(i).append("\":").append(1000 + i);
        }
        body.append("}}");
        MockMvc mockMvc = buildMockMvc();

        // 调用
        mockMvc.perform(post(KEEP_ALIVE_URL).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                // 断言：不限制条数，全部写入
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
        verify(devicePropertyService, times(101)).updateGatewayServerStartTime(any(), any());
        verify(devicePropertyService).updateGatewayServerStartTime(eq("server-100"), eq(1100L));
    }

    /**
     * 构建经过 HTTP 消息转换、参数校验的 MockMvc：控制器使用 CGLIB 方法校验代理，与容器中的 Bean 一致
     */
    private MockMvc buildMockMvc() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setProxyTargetClass(true);
        processor.setValidator(Validation.buildDefaultValidatorFactory().getValidator());
        processor.afterPropertiesSet();
        Object controller = processor.postProcessAfterInitialization(deviceApiImpl, "ioTDeviceApiImpl");
        assertTrue(AopUtils.isCglibProxy(controller));
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

}
