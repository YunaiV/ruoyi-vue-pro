package cn.iocoder.yudao.module.iot.service.device.property;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDevicePropertyDAO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_PROPERTY_HISTORY_TIMES_INVALID;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
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
    private IotProductService productService;
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDevicePropertyDAO devicePropertyDAO;
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
        verify(devicePropertyDAO, never()).insert(any(), any(), anyLong(), anyLong());
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

        // mock 方法
        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(temperature));
        when(thingModelService.convertThingModelPropertyValue(temperature, "abc")).thenReturn(null);

        // 调用，并断言：不会抛出异常
        assertDoesNotThrow(() -> service.saveDeviceProperty(device, message));

        // 断言：没有合法属性，不会写入 TDengine 与 Redis
        verify(devicePropertyDAO, never()).insert(any(), any(), anyLong(), anyLong());
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

        // mock 方法
        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(thingModel));

        // 调用，并断言：不会抛出异常
        assertDoesNotThrow(() -> service.saveDeviceProperty(device, message));

        // 断言：跳过空值，不会转换属性值，也不会写入 TDengine 与 Redis
        verify(thingModelService, never()).convertThingModelPropertyValue(any(), any());
        verify(devicePropertyDAO, never()).insert(any(), any(), anyLong(), anyLong());
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

        // mock 方法
        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(thingModel));
        when(thingModelService.convertThingModelPropertyValue(thingModel, true)).thenReturn((byte) 1);

        // 调用，并断言：非字符串 key 不影响其它合法属性
        assertDoesNotThrow(() -> service.saveDeviceProperty(device, message));

        // 断言：只写入合法属性
        Map<String, Object> dbProperties = captureMapperInsertProperties();
        assertEquals(1, dbProperties.size());
        assertEquals((byte) 1, dbProperties.get("PowerSwitch"));
    }

    @Test
    public void testDefineDevicePropertyData_success() {
        // 准备参数：只有属性类型的物模型，才需要定义存储结构
        Long productId = 2L;
        IotProductDO product = IotProductDO.builder().id(productId).build();
        IotThingModelDO temperature = buildThingModel("Temperature", IotDataSpecsDataTypeEnum.FLOAT.getDataType());
        temperature.setType(IotThingModelTypeEnum.PROPERTY.getType());
        IotThingModelDO alarm = buildThingModel("Alarm", IotDataSpecsDataTypeEnum.INT.getDataType());
        alarm.setType(IotThingModelTypeEnum.EVENT.getType());

        // mock 方法
        when(productService.validateProductExists(productId)).thenReturn(product);
        when(thingModelService.getThingModelListByProductId(productId)).thenReturn(Arrays.asList(temperature, alarm));

        // 调用
        service.defineDevicePropertyData(productId);

        // 断言：只传入属性物模型，由存储 DAO 完成字段映射与差异比较
        verify(devicePropertyDAO).defineProductPropertyTable(eq(productId), eq(singletonList(temperature)));
    }

    @Test
    public void testDefineDevicePropertyData_productNotExists() {
        // 准备参数
        Long productId = 2L;

        // mock 方法：产品不存在
        when(productService.validateProductExists(productId))
                .thenThrow(exception(PRODUCT_NOT_EXISTS));

        // 调用，并断言
        assertServiceException(() -> service.defineDevicePropertyData(productId), PRODUCT_NOT_EXISTS);
        verify(devicePropertyDAO, never()).defineProductPropertyTable(any(), any());
    }

    @Test
    public void testDeleteDevicePropertyData_success() {
        // 准备参数
        Long productId = 2L;

        // 调用
        service.deleteDevicePropertyData(productId);

        // 断言：委托给存储 DAO 删除属性表，不依赖 MySQL 产品是否存在
        verify(devicePropertyDAO).deleteProductPropertyTable(eq(productId));
        verifyNoInteractions(productService, thingModelService);
    }

    @Test
    public void testDeleteDevicePropertyData_failed() {
        // 准备参数
        Long productId = 2L;
        // mock 方法：存储 DAO 删除失败
        RuntimeException failure = new RuntimeException("drop property table failed");
        doThrow(failure).when(devicePropertyDAO).deleteProductPropertyTable(productId);

        // 调用，并断言：异常原样抛出，由产品删除流程中止
        assertSame(failure, assertThrows(RuntimeException.class, () -> service.deleteDevicePropertyData(productId)));
    }

    @Test
    public void testSaveDeviceProperty_success() {
        // 准备参数
        IotDeviceDO device = buildDevice();
        IotThingModelDO thingModel = buildThingModel("Temperature", IotDataSpecsDataTypeEnum.INT.getDataType());
        Map<String, Object> params = new HashMap<>();
        params.put("Temperature", 36);
        IotDeviceMessage message = buildMessage(params);

        // mock 方法
        when(thingModelService.getThingModelListByProductIdFromCache(device.getProductId()))
                .thenReturn(singletonList(thingModel));
        when(thingModelService.convertThingModelPropertyValue(thingModel, 36)).thenReturn(36);

        // 调用
        long before = System.currentTimeMillis();
        service.saveDeviceProperty(device, message);
        long after = System.currentTimeMillis();

        // 断言：ts 为存储时间，reportTime 为上报时间
        ArgumentCaptor<Long> tsCaptor = ArgumentCaptor.forClass(Long.class);
        verify(devicePropertyDAO).insert(eq(device), eq(MapUtil.of("Temperature", 36)), tsCaptor.capture(),
                eq(LocalDateTimeUtil.toEpochMilli(message.getReportTime())));
        assertTrue(tsCaptor.getValue() >= before && tsCaptor.getValue() <= after);
    }

    // ========== getHistoryDevicePropertyList ==========

    @Test
    public void testGetHistoryDevicePropertyList_success() {
        // 准备参数
        IotDevicePropertyHistoryListReqVO reqVO = buildHistoryReqVO("Temperature",
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 2, 0, 0));
        IotDevicePropertyRespVO respVO = new IotDevicePropertyRespVO();
        respVO.setValue(36);
        respVO.setUpdateTime(1L);

        // mock 方法
        when(deviceService.getDevice(eq(1L))).thenReturn(buildDevice());
        when(devicePropertyDAO.selectHistoryList(eq(2L), same(reqVO))).thenReturn(singletonList(respVO));

        // 调用
        List<IotDevicePropertyRespVO> list = service.getHistoryDevicePropertyList(reqVO);

        // 断言：按设备的产品编号查询时序库；使用按租户、逻辑删除过滤的 getDevice，而不是忽略租户的缓存查询
        assertEquals(singletonList(respVO), list);
        verify(deviceService, never()).getDeviceFromCache(anyLong());
    }

    @Test
    public void testGetHistoryDevicePropertyList_timesNull() {
        // 准备参数
        IotDevicePropertyHistoryListReqVO reqVO = buildHistoryReqVO("Temperature");
        reqVO.setTimes(null);

        // 调用，并断言
        assertServiceException(() -> service.getHistoryDevicePropertyList(reqVO), DEVICE_PROPERTY_HISTORY_TIMES_INVALID);
        verifyNoInteractions(deviceService, devicePropertyDAO);
    }

    @Test
    public void testGetHistoryDevicePropertyList_timesLengthInvalid() {
        // 准备参数
        IotDevicePropertyHistoryListReqVO reqVO = buildHistoryReqVO("Temperature", LocalDateTime.now());

        // 调用，并断言
        assertServiceException(() -> service.getHistoryDevicePropertyList(reqVO), DEVICE_PROPERTY_HISTORY_TIMES_INVALID);
        verifyNoInteractions(deviceService, devicePropertyDAO);
    }

    @Test
    public void testGetHistoryDevicePropertyList_timeNull() {
        // 准备参数：开始时间为空、结束时间为空
        IotDevicePropertyHistoryListReqVO reqVO1 = buildHistoryReqVO("Temperature", null, LocalDateTime.now());
        IotDevicePropertyHistoryListReqVO reqVO2 = buildHistoryReqVO("Temperature", LocalDateTime.now(), null);

        // 调用，并断言：返回空，不查询设备与时序库
        assertTrue(service.getHistoryDevicePropertyList(reqVO1).isEmpty());
        assertTrue(service.getHistoryDevicePropertyList(reqVO2).isEmpty());
        verifyNoInteractions(deviceService, devicePropertyDAO);
    }

    @Test
    public void testGetHistoryDevicePropertyList_identifierInvalid() {
        // 准备参数：保留字段、非法格式
        LocalDateTime now = LocalDateTime.now();
        IotDevicePropertyHistoryListReqVO reqVO1 = buildHistoryReqVO("reportTime", now, now);
        IotDevicePropertyHistoryListReqVO reqVO2 = buildHistoryReqVO("ts", now, now);
        IotDevicePropertyHistoryListReqVO reqVO3 = buildHistoryReqVO("a` OR 1=1", now, now);

        // 调用，并断言：返回空，不查询设备与时序库
        assertTrue(service.getHistoryDevicePropertyList(reqVO1).isEmpty());
        assertTrue(service.getHistoryDevicePropertyList(reqVO2).isEmpty());
        assertTrue(service.getHistoryDevicePropertyList(reqVO3).isEmpty());
        verifyNoInteractions(deviceService, devicePropertyDAO);
    }

    @Test
    public void testGetHistoryDevicePropertyList_deviceNotExists() {
        // 准备参数
        LocalDateTime now = LocalDateTime.now();
        IotDevicePropertyHistoryListReqVO reqVO = buildHistoryReqVO("Temperature", now, now);

        // mock 方法：设备不存在、已删除，或者不属于当前租户
        when(deviceService.getDevice(eq(1L))).thenReturn(null);

        // 调用，并断言：返回空，不查询时序库
        assertTrue(service.getHistoryDevicePropertyList(reqVO).isEmpty());
        verifyNoInteractions(devicePropertyDAO);
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
     * 构造属性历史查询条件，设备编号固定为 1
     */
    private IotDevicePropertyHistoryListReqVO buildHistoryReqVO(String identifier, LocalDateTime... times) {
        IotDevicePropertyHistoryListReqVO reqVO = new IotDevicePropertyHistoryListReqVO();
        reqVO.setDeviceId(1L);
        reqVO.setIdentifier(identifier);
        reqVO.setTimes(times);
        return reqVO;
    }

    /**
     * 抓取 DAO.insert 的 properties 入参
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> captureMapperInsertProperties() {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(devicePropertyDAO).insert(any(IotDeviceDO.class), captor.capture(), anyLong(), anyLong());
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
