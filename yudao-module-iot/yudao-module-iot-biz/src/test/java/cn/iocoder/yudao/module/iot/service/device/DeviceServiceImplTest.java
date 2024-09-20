package cn.iocoder.yudao.module.iot.service.device;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DeviceServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(DeviceServiceImpl.class)
public class DeviceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DeviceServiceImpl deviceService;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Test
    public void testCreateDevice_success() {
        // 准备参数
        IotDeviceSaveReqVO createReqVO = randomPojo(IotDeviceSaveReqVO.class).setId(null);

        // 调用
        Long deviceId = deviceService.createDevice(createReqVO);
        // 断言
        assertNotNull(deviceId);
        // 校验记录的属性是否正确
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        assertPojoEquals(createReqVO, device, "id");
    }

    @Test
    public void testUpdateDevice_success() {
        // mock 数据
        IotDeviceDO dbDevice = randomPojo(IotDeviceDO.class);
        deviceMapper.insert(dbDevice);// @Sql: 先插入出一条存在的数据
        // 准备参数
        IotDeviceSaveReqVO updateReqVO = randomPojo(IotDeviceSaveReqVO.class, o -> {
            o.setId(dbDevice.getId()); // 设置更新的 ID
        });

        // 调用
        deviceService.updateDevice(updateReqVO);
        // 校验是否更新正确
        IotDeviceDO device = deviceMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, device);
    }

    @Test
    public void testUpdateDevice_notExists() {
        // 准备参数
        IotDeviceSaveReqVO updateReqVO = randomPojo(IotDeviceSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> deviceService.updateDevice(updateReqVO), DEVICE_NOT_EXISTS);
    }

    @Test
    public void testDeleteDevice_success() {
        // mock 数据
        IotDeviceDO dbDevice = randomPojo(IotDeviceDO.class);
        deviceMapper.insert(dbDevice);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDevice.getId();

        // 调用
        deviceService.deleteDevice(id);
       // 校验数据不存在了
       assertNull(deviceMapper.selectById(id));
    }

    @Test
    public void testDeleteDevice_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> deviceService.deleteDevice(id), DEVICE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDevicePage() {
       // mock 数据
       IotDeviceDO dbDevice = randomPojo(IotDeviceDO.class, o -> { // 等会查询到
           o.setDeviceKey(null);
           o.setDeviceName(null);
           o.setProductId(null);
           o.setProductKey(null);
           o.setDeviceType(null);
           o.setNickname(null);
           o.setGatewayId(null);
           o.setStatus(null);
           o.setStatusLastUpdateTime(null);
           o.setLastOnlineTime(null);
           o.setLastOfflineTime(null);
           o.setActiveTime(null);
           o.setIp(null);
           o.setFirmwareVersion(null);
           o.setDeviceSecret(null);
           o.setMqttClientId(null);
           o.setMqttUsername(null);
           o.setMqttPassword(null);
           o.setAuthType(null);
           o.setLatitude(null);
           o.setLongitude(null);
           o.setAreaId(null);
           o.setAddress(null);
           o.setSerialNumber(null);
           o.setCreateTime(null);
       });
       deviceMapper.insert(dbDevice);
       // 测试 deviceKey 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setDeviceKey(null)));
       // 测试 deviceName 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setDeviceName(null)));
       // 测试 productId 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setProductId(null)));
       // 测试 productKey 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setProductKey(null)));
       // 测试 deviceType 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setDeviceType(null)));
       // 测试 nickname 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setNickname(null)));
       // 测试 gatewayId 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setGatewayId(null)));
       // 测试 status 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setStatus(null)));
       // 测试 statusLastUpdateTime 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setStatusLastUpdateTime(null)));
       // 测试 lastOnlineTime 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setLastOnlineTime(null)));
       // 测试 lastOfflineTime 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setLastOfflineTime(null)));
       // 测试 activeTime 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setActiveTime(null)));
       // 测试 ip 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setIp(null)));
       // 测试 firmwareVersion 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setFirmwareVersion(null)));
       // 测试 deviceSecret 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setDeviceSecret(null)));
       // 测试 mqttClientId 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setMqttClientId(null)));
       // 测试 mqttUsername 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setMqttUsername(null)));
       // 测试 mqttPassword 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setMqttPassword(null)));
       // 测试 authType 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setAuthType(null)));
       // 测试 latitude 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setLatitude(null)));
       // 测试 longitude 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setLongitude(null)));
       // 测试 areaId 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setAreaId(null)));
       // 测试 address 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setAddress(null)));
       // 测试 serialNumber 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setSerialNumber(null)));
       // 测试 createTime 不匹配
       deviceMapper.insert(cloneIgnoreId(dbDevice, o -> o.setCreateTime(null)));
       // 准备参数
       IotDevicePageReqVO reqVO = new IotDevicePageReqVO();
       reqVO.setDeviceKey(null);
       reqVO.setDeviceName(null);
       reqVO.setProductId(null);
       reqVO.setProductKey(null);
       reqVO.setDeviceType(null);
       reqVO.setNickname(null);
       reqVO.setGatewayId(null);
       reqVO.setStatus(null);
       reqVO.setStatusLastUpdateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setLastOnlineTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setLastOfflineTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActiveTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setIp(null);
       reqVO.setFirmwareVersion(null);
       reqVO.setDeviceSecret(null);
       reqVO.setMqttClientId(null);
       reqVO.setMqttUsername(null);
       reqVO.setMqttPassword(null);
       reqVO.setAuthType(null);
       reqVO.setLatitude(null);
       reqVO.setLongitude(null);
       reqVO.setAreaId(null);
       reqVO.setAddress(null);
       reqVO.setSerialNumber(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<IotDeviceDO> pageResult = deviceService.getDevicePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDevice, pageResult.getList().get(0));
    }

}