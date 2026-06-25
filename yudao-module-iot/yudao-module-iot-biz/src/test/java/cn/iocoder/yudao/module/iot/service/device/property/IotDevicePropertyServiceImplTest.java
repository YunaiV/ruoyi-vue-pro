package cn.iocoder.yudao.module.iot.service.device.property;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDevicePropertyMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link IotDevicePropertyServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class IotDevicePropertyServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDevicePropertyServiceImpl service;

    @Mock
    private IotThingModelService thingModelService;
    @Mock
    private IotDevicePropertyMapper devicePropertyMapper;
    @Mock
    private DevicePropertyRedisDAO deviceDataRedisDAO;

    @Test
    public void testSaveDeviceProperty_identifierCaseInsensitive() {
        // 准备参数：物模型 identifier 是 "LightStatus"，设备上报的 key 是 "LIGHTSTATUS"（全大写）
        IotDeviceDO device = buildDevice();
        IotThingModelDO thingModel = buildThingModel("LightStatus", IotDataSpecsDataTypeEnum.INT.getDataType());
        Map<String, Object> params = new HashMap<>();
        params.put("LIGHTSTATUS", 100);
        IotDeviceMessage message = buildMessage(params);

        // mock 行为
        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(thingModel));
        when(thingModelService.convertThingModelPropertyValue(thingModel, 100)).thenReturn(100);

        // 调用
        service.saveDeviceProperty(device, message);

        // 断言：properties 落库 / 入缓存时 key 应为物模型 identifier "LightStatus"，而不是上报的 "LIGHTSTATUS"
        Map<String, Object> dbProperties = captureMapperInsertProperties();
        assertTrue(dbProperties.containsKey("LightStatus"));
        assertFalse(dbProperties.containsKey("LIGHTSTATUS"));
        assertEquals(100, dbProperties.get("LightStatus"));

        Map<String, IotDevicePropertyDO> redisProperties = captureRedisPutAllProperties(device.getId());
        assertTrue(redisProperties.containsKey("LightStatus"));
        assertFalse(redisProperties.containsKey("LIGHTSTATUS"));
    }

    @Test
    public void testSaveDeviceProperty_identifierNotInThingModel() {
        // 准备参数：上报的 key 在物模型里完全不存在（连忽略大小写都匹配不到）
        IotDeviceDO device = buildDevice();
        IotThingModelDO thingModel = buildThingModel("LightStatus", IotDataSpecsDataTypeEnum.INT.getDataType());
        Map<String, Object> params = new HashMap<>();
        params.put("UnknownProperty", 1);
        IotDeviceMessage message = buildMessage(params);

        // mock 行为
        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(thingModel));

        // 调用
        service.saveDeviceProperty(device, message);

        // 断言：没有合法属性，不会写入 TDengine 与 Redis
        verify(devicePropertyMapper, never()).insert(any(), any(), anyLong(), anyLong());
        verify(deviceDataRedisDAO, never()).putAll(anyLong(), any());
    }

    @Test
    public void testSaveDeviceProperty_convertValueFailed() {
        // 准备参数：物模型存在，但是属性值无法按物模型转换
        IotDeviceDO device = buildDevice();
        IotThingModelDO temperature = buildThingModel("Temperature", IotDataSpecsDataTypeEnum.INT.getDataType());
        Map<String, Object> params = new HashMap<>();
        params.put("Temperature", "abc");
        IotDeviceMessage message = buildMessage(params);

        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(temperature));
        when(thingModelService.convertThingModelPropertyValue(temperature, "abc")).thenReturn(null);

        assertDoesNotThrow(() -> service.saveDeviceProperty(device, message));

        verify(devicePropertyMapper, never()).insert(any(), any(), anyLong(), anyLong());
        verify(deviceDataRedisDAO, never()).putAll(anyLong(), any());
    }

    @Test
    public void testSaveDeviceProperty_skipNullValue() {
        // 准备参数：属性值为空，不能写入 TDengine 与 Redis
        IotDeviceDO device = buildDevice();
        IotThingModelDO thingModel = buildThingModel("Temperature", IotDataSpecsDataTypeEnum.INT.getDataType());
        Map<String, Object> params = new HashMap<>();
        params.put("Temperature", null);
        IotDeviceMessage message = buildMessage(params);

        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(thingModel));

        assertDoesNotThrow(() -> service.saveDeviceProperty(device, message));

        verify(thingModelService, never()).convertThingModelPropertyValue(any(), any());
        verify(devicePropertyMapper, never()).insert(any(), any(), anyLong(), anyLong());
        verify(deviceDataRedisDAO, never()).putAll(anyLong(), any());
    }

    @Test
    public void testSaveDeviceProperty_skipInvalidKeyType() {
        // 准备参数：Map 中包含非字符串 key，不能因为强转失败影响其它合法属性
        IotDeviceDO device = buildDevice();
        IotThingModelDO thingModel = buildThingModel("PowerSwitch", IotDataSpecsDataTypeEnum.BOOL.getDataType());
        Map<Object, Object> params = new HashMap<>();
        params.put(123, 1);
        params.put("PowerSwitch", true);
        IotDeviceMessage message = buildMessage(params);

        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(thingModel));
        when(thingModelService.convertThingModelPropertyValue(thingModel, true)).thenReturn((byte) 1);

        assertDoesNotThrow(() -> service.saveDeviceProperty(device, message));

        Map<String, Object> dbProperties = captureMapperInsertProperties();
        assertEquals(1, dbProperties.size());
        assertEquals((byte) 1, dbProperties.get("PowerSwitch"));
    }

    // ========== 辅助方法 ==========

    /**
     * 构造一个最简 IotDeviceDO，只设置测试需要的 id 与 productId
     */
    private IotDeviceDO buildDevice() {
        return IotDeviceDO.builder().id(1L).productId(2L).build();
    }

    /**
     * 构造物模型；只填 saveDeviceProperty 链路用到的 identifier + property.dataType
     */
    private IotThingModelDO buildThingModel(String identifier, String dataType) {
        ThingModelProperty property = new ThingModelProperty();
        property.setIdentifier(identifier);
        property.setDataType(dataType);
        return IotThingModelDO.builder().identifier(identifier).property(property).build();
    }

    /**
     * 构造一条属性上报消息
     */
    private IotDeviceMessage buildMessage(Map<?, ?> params) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams(params);
        message.setReportTime(LocalDateTime.now());
        return message;
    }

    /**
     * 抓取 mapper.insert 的 properties 入参
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> captureMapperInsertProperties() {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(devicePropertyMapper).insert(any(IotDeviceDO.class), captor.capture(), anyLong(), anyLong());
        return captor.getValue();
    }

    /**
     * 抓取 redisDAO.putAll 的 properties 入参
     */
    @SuppressWarnings("unchecked")
    private Map<String, IotDevicePropertyDO> captureRedisPutAllProperties(Long deviceId) {
        ArgumentCaptor<Map<String, IotDevicePropertyDO>> captor = ArgumentCaptor.forClass(Map.class);
        verify(deviceDataRedisDAO).putAll(eq(deviceId), captor.capture());
        return captor.getValue();
    }

}
