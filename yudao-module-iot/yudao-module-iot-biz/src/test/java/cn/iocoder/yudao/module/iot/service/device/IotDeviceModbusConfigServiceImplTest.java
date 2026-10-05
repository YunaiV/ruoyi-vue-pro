package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceModbusConfigMapper;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link IotDeviceModbusConfigServiceImpl} 的单元测试
 */
public class IotDeviceModbusConfigServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceModbusConfigServiceImpl modbusConfigService;

    @Mock
    private IotDeviceModbusConfigMapper modbusConfigMapper;
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotProductService productService;

    // 设备 → 产品（协议类型决定走 Client 还是 Server 分支）
    private void mockDeviceAndProduct(String protocolType) {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(1L);
        device.setProductId(2L);
        when(deviceService.validateDeviceExists(1L)).thenReturn(device);

        IotProductDO product = new IotProductDO();
        product.setId(2L);
        product.setProtocolType(protocolType);
        when(productService.getProduct(2L)).thenReturn(product);
    }

    // Client 模式的请求参数
    private IotDeviceModbusConfigSaveReqVO createClientReqVO(String ip, Integer port,
                                                               Integer timeout, Integer retryInterval) {
        IotDeviceModbusConfigSaveReqVO reqVO = new IotDeviceModbusConfigSaveReqVO();
        reqVO.setDeviceId(1L);
        reqVO.setSlaveId(1);
        reqVO.setStatus(0);
        reqVO.setIp(ip);
        reqVO.setPort(port);
        reqVO.setTimeout(timeout);
        reqVO.setRetryInterval(retryInterval);
        return reqVO;
    }

    // Server 模式的请求参数
    private IotDeviceModbusConfigSaveReqVO createServerReqVO(Integer mode, Integer frameFormat) {
        IotDeviceModbusConfigSaveReqVO reqVO = new IotDeviceModbusConfigSaveReqVO();
        reqVO.setDeviceId(1L);
        reqVO.setSlaveId(1);
        reqVO.setStatus(0);
        reqVO.setMode(mode);
        reqVO.setFrameFormat(frameFormat);
        return reqVO;
    }

    @Test
    public void testSaveDeviceModbusConfig_clientIpNull() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType());
        assertServiceException(() -> modbusConfigService.saveDeviceModbusConfig(
                createClientReqVO(null, 502, 3000, 1000)), DEVICE_MODBUS_CONFIG_IP_REQUIRED);
    }

    @Test
    public void testSaveDeviceModbusConfig_clientPortNull() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType());
        assertServiceException(() -> modbusConfigService.saveDeviceModbusConfig(
                createClientReqVO("127.0.0.1", null, 3000, 1000)), DEVICE_MODBUS_CONFIG_PORT_REQUIRED);
    }

    @Test
    public void testSaveDeviceModbusConfig_clientTimeoutNull() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType());
        assertServiceException(() -> modbusConfigService.saveDeviceModbusConfig(
                createClientReqVO("127.0.0.1", 502, null, 1000)), DEVICE_MODBUS_CONFIG_TIMEOUT_REQUIRED);
    }

    @Test
    public void testSaveDeviceModbusConfig_clientRetryIntervalNull() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType());
        assertServiceException(() -> modbusConfigService.saveDeviceModbusConfig(
                createClientReqVO("127.0.0.1", 502, 3000, null)), DEVICE_MODBUS_CONFIG_RETRY_INTERVAL_REQUIRED);
    }

    @Test
    public void testSaveDeviceModbusConfig_serverModeNull() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_SERVER.getType());
        assertServiceException(() -> modbusConfigService.saveDeviceModbusConfig(
                createServerReqVO(null, 1)), DEVICE_MODBUS_CONFIG_MODE_REQUIRED);
    }

    @Test
    public void testSaveDeviceModbusConfig_serverFrameFormatNull() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_SERVER.getType());
        assertServiceException(() -> modbusConfigService.saveDeviceModbusConfig(
                createServerReqVO(1, null)), DEVICE_MODBUS_CONFIG_FRAME_FORMAT_REQUIRED);
    }

    // 没有配置 → 走新增分支
    @Test
    public void testSaveDeviceModbusConfig_clientSuccessInsert() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType());
        when(modbusConfigMapper.selectByDeviceId(1L)).thenReturn(null);

        modbusConfigService.saveDeviceModbusConfig(createClientReqVO("127.0.0.1", 502, 3000, 1000));

        verify(modbusConfigMapper).insert(any(IotDeviceModbusConfigDO.class));
        verify(modbusConfigMapper, never()).updateById(any(IotDeviceModbusConfigDO.class));
    }

    // 已有配置 → 走更新分支
    @Test
    public void testSaveDeviceModbusConfig_serverSuccessUpdate() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MODBUS_TCP_SERVER.getType());
        IotDeviceModbusConfigDO existConfig = new IotDeviceModbusConfigDO();
        existConfig.setId(9L);
        when(modbusConfigMapper.selectByDeviceId(1L)).thenReturn(existConfig);

        modbusConfigService.saveDeviceModbusConfig(createServerReqVO(1, 1));

        verify(modbusConfigMapper).updateById(any(IotDeviceModbusConfigDO.class));
        verify(modbusConfigMapper, never()).insert(any(IotDeviceModbusConfigDO.class));
    }

    // 非 Modbus 协议（如 mqtt）不校验这些必填参数，避免误伤
    @Test
    public void testSaveDeviceModbusConfig_mqttProtocolNoValidate() {
        mockDeviceAndProduct(IotProtocolTypeEnum.MQTT.getType());
        when(modbusConfigMapper.selectByDeviceId(1L)).thenReturn(null);

        // ip / port / timeout / retryInterval / mode / frameFormat 全为空也应保存成功
        IotDeviceModbusConfigSaveReqVO reqVO = new IotDeviceModbusConfigSaveReqVO();
        reqVO.setDeviceId(1L);
        reqVO.setSlaveId(1);
        reqVO.setStatus(0);
        modbusConfigService.saveDeviceModbusConfig(reqVO);

        verify(modbusConfigMapper).insert(any(IotDeviceModbusConfigDO.class));
    }

}
