package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotGatewayServerAliveReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceOpcUaConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceOpcUaPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.opcuaserver.IotOpcUaServerConfigService;
import cn.iocoder.yudao.module.iot.service.opcuaserver.IotOpcUaServerNodeService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
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

    private static final Integer AUTH_USERNAME = IotOpcUaAuthTypeEnum.USERNAME.getType();
    private static final Integer COLLECT_SUBSCRIBE = IotOpcUaCollectModeEnum.SUBSCRIBE.getMode();

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
    @Mock
    private IotDeviceOpcUaConfigService opcUaConfigService;
    @Mock
    private IotDeviceOpcUaPointService opcUaPointService;
    @Mock
    private IotOpcUaServerConfigService opcUaServerConfigService;
    @Mock
    private IotOpcUaServerNodeService opcUaServerNodeService;

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

    // ========== OPC UA 配置列表 ==========

    @Test
    public void testGetOpcUaDeviceConfigList() {
        // 准备参数：只要 OPC UA Client 协议
        IotOpcUaDeviceConfigListReqDTO reqDTO = new IotOpcUaDeviceConfigListReqDTO().setStatus(0)
                .setProtocolType("opc_ua_client");
        // mock 数据：设备 1（OPC UA，有点位）、设备 2（OPC UA，没有点位）、设备 3（Modbus）、设备 4（产品不存在）
        //   设备 1 有两条配置（并发写入前的历史数据），只返回编号最小的一条
        when(opcUaConfigService.getDeviceOpcUaConfigList(same(reqDTO))).thenReturn(Arrays.asList(
                newOpcUaConfig(11L, 1L, AUTH_USERNAME, "plc-reader"), newOpcUaConfig(12L, 1L, AUTH_USERNAME, "plc-other"),
                newOpcUaConfig(21L, 2L, AUTH_USERNAME, "plc-reader"), newOpcUaConfig(31L, 3L, AUTH_USERNAME, "plc-reader"),
                newOpcUaConfig(41L, 4L, AUTH_USERNAME, "plc-reader"), newOpcUaConfig(51L, 5L, AUTH_USERNAME, "plc-reader")));
        Map<Long, IotDeviceDO> deviceMap = new LinkedHashMap<>();
        deviceMap.put(1L, newDevice(1L, 100L));
        deviceMap.put(2L, newDevice(2L, 100L));
        deviceMap.put(3L, newDevice(3L, 300L));
        deviceMap.put(4L, newDevice(4L, 400L));
        // 设备 5 已删除：不在 deviceMap 中
        when(deviceService.getDeviceMap(any())).thenReturn(deviceMap);
        Map<Long, IotProductDO> productMap = new LinkedHashMap<>();
        productMap.put(100L, IotProductDO.builder().id(100L).protocolType("opc_ua_client").build());
        productMap.put(300L, IotProductDO.builder().id(300L).protocolType("modbus_tcp_client").build());
        when(productService.getProductMap(any())).thenReturn(productMap);
        // mock 数据：设备 1 的点位：正常、重复（同一物模型属性）、物模型已删除、物模型属于其它产品
        Map<Long, List<IotDeviceOpcUaPointDO>> pointMap = new LinkedHashMap<>();
        IotDeviceOpcUaPointDO writablePoint = newOpcUaPoint(101L, 1L, 1001L, "nsu=urn:plc;s=Temp");
        writablePoint.setWritable(true);
        pointMap.put(1L, Arrays.asList(writablePoint,
                newOpcUaPoint(102L, 1L, 1001L, "ns=2;s=Temp"),
                newOpcUaPoint(103L, 1L, 1002L, "ns=2;s=Deleted"),
                newOpcUaPoint(104L, 1L, 3001L, "ns=2;s=Other")));
        when(opcUaPointService.getEnabledDeviceOpcUaPointMapByDeviceIds(any())).thenReturn(pointMap);
        Map<Long, IotThingModelDO> thingModelMap = new LinkedHashMap<>();
        thingModelMap.put(1001L, IotThingModelDO.builder().id(1001L).productId(100L).identifier("temperature")
                .property(newProperty(IotThingModelAccessModeEnum.READ_WRITE.getMode())).build());
        thingModelMap.put(3001L, IotThingModelDO.builder().id(3001L).productId(300L).identifier("other").build());
        when(opcUaPointService.getPropertyThingModelMap(any())).thenReturn(thingModelMap);

        // 调用
        CommonResult<List<IotOpcUaDeviceConfigRespDTO>> result = deviceApi.getOpcUaDeviceConfigList(reqDTO);
        // 断言：协议不匹配、产品不存在、设备已删除的不返回；同一设备只返回一条
        assertTrue(result.isSuccess());
        List<IotOpcUaDeviceConfigRespDTO> list = result.getData();
        assertEquals(2, list.size());
        IotOpcUaDeviceConfigRespDTO config1 = list.get(0);
        assertEquals(1L, config1.getDeviceId());
        assertEquals(1L, config1.getTenantId());
        assertEquals("pk-1", config1.getProductKey());
        assertEquals("dn-1", config1.getDeviceName());
        assertEquals(AUTH_USERNAME, config1.getAuthType());
        assertEquals("plc-reader", config1.getCredentialAlias()); // 只返回凭据别名，不含口令
        // 断言：点位只保留物模型有效、属于本产品的；同一物模型属性只保留编号最小的
        assertEquals(1, config1.getPoints().size());
        assertEquals(101L, config1.getPoints().get(0).getId());
        assertEquals("temperature", config1.getPoints().get(0).getIdentifier());
        assertEquals("nsu=urn:plc;s=Temp", config1.getPoints().get(0).getNodeId());
        assertTrue(config1.getPoints().get(0).getWritable());
        // 断言：没有点位的设备也返回（维护会话与上下线）
        IotOpcUaDeviceConfigRespDTO config2 = list.get(1);
        assertEquals(2L, config2.getDeviceId());
        assertTrue(config2.getPoints().isEmpty());
    }

    @Test
    public void testGetOpcUaDeviceConfigList_writableReadOnlyThingModel() {
        // 准备参数
        IotOpcUaDeviceConfigListReqDTO reqDTO = new IotOpcUaDeviceConfigListReqDTO();
        // mock 数据：设备 1 的两个点位都开启了可写；speed 的物模型属性后来改为只读，mode 的物模型属性缺少定义
        when(opcUaConfigService.getDeviceOpcUaConfigList(same(reqDTO))).thenReturn(
                Collections.singletonList(newOpcUaConfig(11L, 1L, AUTH_USERNAME, "plc-reader")));
        when(deviceService.getDeviceMap(any())).thenReturn(Collections.singletonMap(1L, newDevice(1L, 100L)));
        when(productService.getProductMap(any())).thenReturn(
                Collections.singletonMap(100L, IotProductDO.builder().id(100L).protocolType("opc_ua_client").build()));
        IotDeviceOpcUaPointDO speedPoint = newOpcUaPoint(101L, 1L, 1001L, "ns=2;s=Speed");
        speedPoint.setWritable(true);
        IotDeviceOpcUaPointDO modePoint = newOpcUaPoint(102L, 1L, 1002L, "ns=2;s=Mode");
        modePoint.setWritable(true);
        when(opcUaPointService.getEnabledDeviceOpcUaPointMapByDeviceIds(any())).thenReturn(
                Collections.singletonMap(1L, Arrays.asList(speedPoint, modePoint)));
        Map<Long, IotThingModelDO> thingModelMap = new LinkedHashMap<>();
        thingModelMap.put(1001L, IotThingModelDO.builder().id(1001L).productId(100L).identifier("speed")
                .property(newProperty(IotThingModelAccessModeEnum.READ_ONLY.getMode())).build());
        thingModelMap.put(1002L, IotThingModelDO.builder().id(1002L).productId(100L).identifier("mode").build());
        when(opcUaPointService.getPropertyThingModelMap(any())).thenReturn(thingModelMap);

        // 调用
        CommonResult<List<IotOpcUaDeviceConfigRespDTO>> result = deviceApi.getOpcUaDeviceConfigList(reqDTO);
        // 断言：下发给网关的可写与物模型当前的读写模式取交集，点位上残留的 true 不下发
        List<IotOpcUaPointRespDTO> points = result.getData().get(0).getPoints();
        assertEquals(2, points.size());
        assertFalse(points.get(0).getWritable());
        assertFalse(points.get(1).getWritable());
    }

    @Test
    public void testGetOpcUaDeviceConfigList_empty() {
        // mock 方法：没有配置
        IotOpcUaDeviceConfigListReqDTO reqDTO = new IotOpcUaDeviceConfigListReqDTO();
        when(opcUaConfigService.getDeviceOpcUaConfigList(same(reqDTO))).thenReturn(List.of());

        // 调用，并断言
        CommonResult<List<IotOpcUaDeviceConfigRespDTO>> result = deviceApi.getOpcUaDeviceConfigList(reqDTO);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        verifyNoInteractions(deviceService, opcUaPointService);
    }

    @Test
    public void testGetOpcUaServerConfigList() {
        // mock 数据：配置 1（租户 1）、配置 2（与配置 1 凭据别名重复）、配置 3（租户 2，节点引用了租户 1 的设备）
        when(opcUaServerConfigService.getEnabledOpcUaServerConfigList()).thenReturn(Arrays.asList(
                newOpcUaServerConfig(1L, 1L, "scada-a"), newOpcUaServerConfig(2L, 1L, "scada-a"),
                newOpcUaServerConfig(3L, 2L, "scada-b")));
        Map<Long, List<IotOpcUaServerNodeDO>> nodeMap = new LinkedHashMap<>();
        nodeMap.put(1L, Arrays.asList(newOpcUaServerNode(11L, 1L, 1L, 1001L, "temperature"),
                newOpcUaServerNode(12L, 1L, 1L, 1001L, "temperature"), // 浏览名称重复
                newOpcUaServerNode(13L, 1L, 1L, 3001L, "other"), // 物模型属于其它产品
                newOpcUaServerNode(14L, 1L, 9L, 1001L, "deleted"))); // 设备已删除
        nodeMap.put(3L, Collections.singletonList(newOpcUaServerNode(31L, 3L, 1L, 1001L, "cross-tenant")));
        when(opcUaServerNodeService.getEnabledOpcUaServerNodeMapByConfigIds(eq(asSet(1L, 2L, 3L)))).thenReturn(nodeMap);
        when(deviceService.getDeviceMap(eq(asSet(1L, 9L)))).thenReturn(Collections.singletonMap(1L, newDevice(1L, 100L)));
        Map<Long, IotThingModelDO> thingModelMap = new LinkedHashMap<>();
        ThingModelProperty property = newProperty(IotThingModelAccessModeEnum.READ_ONLY.getMode());
        property.setDataType("float");
        thingModelMap.put(1001L, IotThingModelDO.builder().id(1001L).productId(100L).identifier("temperature")
                .property(property).build());
        thingModelMap.put(3001L, IotThingModelDO.builder().id(3001L).productId(300L).identifier("other").build());
        when(opcUaServerNodeService.getPropertyThingModelMap(any())).thenReturn(thingModelMap);

        // 调用
        CommonResult<List<IotOpcUaServerConfigRespDTO>> result = deviceApi.getOpcUaServerConfigList();
        // 断言：凭据别名重复的配置只返回编号最小的一条
        assertTrue(result.isSuccess());
        List<IotOpcUaServerConfigRespDTO> list = result.getData();
        assertEquals(2, list.size());
        IotOpcUaServerConfigRespDTO config1 = list.get(0);
        assertEquals(1L, config1.getId());
        assertEquals(1L, config1.getTenantId());
        assertEquals("scada-a", config1.getCredentialAlias()); // 只返回凭据别名，不含口令
        assertEquals(2, config1.getMaxSessions());
        // 断言：节点只保留设备存在、物模型属于本产品、浏览名称不重复的
        assertEquals(1, config1.getNodes().size());
        assertEquals(11L, config1.getNodes().get(0).getId());
        assertEquals("pk-1", config1.getNodes().get(0).getProductKey());
        assertEquals("dn-1", config1.getNodes().get(0).getDeviceName());
        assertEquals("temperature", config1.getNodes().get(0).getIdentifier());
        assertEquals("float", config1.getNodes().get(0).getDataType());
        // 断言：租户隔离，配置 3（租户 2）不能暴露租户 1 的设备
        assertEquals(3L, list.get(1).getId());
        assertTrue(list.get(1).getNodes().isEmpty());
    }

    @Test
    public void testGetOpcUaServerConfigList_empty() {
        // mock 方法：没有配置
        when(opcUaServerConfigService.getEnabledOpcUaServerConfigList()).thenReturn(Collections.emptyList());

        // 调用，并断言
        CommonResult<List<IotOpcUaServerConfigRespDTO>> result = deviceApi.getOpcUaServerConfigList();
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        verifyNoInteractions(deviceService, opcUaServerNodeService);
    }

    private static IotOpcUaServerConfigDO newOpcUaServerConfig(Long id, Long tenantId, String credentialAlias) {
        IotOpcUaServerConfigDO config = IotOpcUaServerConfigDO.builder().id(id).name("config-" + id)
                .credentialAlias(credentialAlias).maxSessions(2).maxSubscriptionsPerSession(5)
                .maxMonitoredItemsPerSession(1000).status(0).build();
        config.setTenantId(tenantId);
        return config;
    }

    private static IotOpcUaServerNodeDO newOpcUaServerNode(Long id, Long configId, Long deviceId, Long thingModelId,
                                                           String browseName) {
        return IotOpcUaServerNodeDO.builder().id(id).configId(configId).deviceId(deviceId).thingModelId(thingModelId)
                .browseName(browseName).status(0).build();
    }

    private static IotDeviceOpcUaConfigDO newOpcUaConfig(Long id, Long deviceId, Integer authType, String credentialAlias) {
        return IotDeviceOpcUaConfigDO.builder().id(id).deviceId(deviceId)
                .endpointUrl("opc.tcp://192.168.1.10:4840/server").securityPolicy("Basic256Sha256")
                .securityMode("SignAndEncrypt").authType(authType).credentialAlias(credentialAlias)
                .timeout(5000).retryInterval(10000).status(0).build();
    }

    private static ThingModelProperty newProperty(String accessMode) {
        ThingModelProperty property = new ThingModelProperty();
        property.setAccessMode(accessMode);
        return property;
    }

    private static IotDeviceOpcUaPointDO newOpcUaPoint(Long id, Long deviceId, Long thingModelId, String nodeId) {
        return IotDeviceOpcUaPointDO.builder().id(id).deviceId(deviceId).thingModelId(thingModelId).nodeId(nodeId)
                .collectMode(COLLECT_SUBSCRIBE).collectInterval(1000).status(0).build();
    }

    private static IotDeviceDO newDevice(Long id, Long productId) {
        IotDeviceDO device = IotDeviceDO.builder().id(id).productId(productId)
                .productKey("pk-" + id).deviceName("dn-" + id).build();
        device.setTenantId(1L);
        return device;
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
