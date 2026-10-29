package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.shadow.IotDeviceShadowService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * {@link IotDeviceServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(IotDeviceServiceImpl.class)
public class IotDeviceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private IotDeviceServiceImpl deviceService;

    @Resource
    private IotDeviceMapper deviceMapper;

    @MockBean
    private IotProductService productService;
    @MockBean
    private IotDeviceGroupService deviceGroupService;
    @MockBean
    private IotDeviceMessageService deviceMessageService;
    @MockBean
    private IotDeviceShadowService deviceShadowService;

    @Test
    public void testDeleteDevice_success() {
        // mock 数据
        IotDeviceDO dbDevice = buildDevice();
        deviceMapper.insert(dbDevice);
        // 准备参数
        Long id = dbDevice.getId();

        // 调用
        deviceService.deleteDevice(id);
        // 断言
        assertNull(deviceMapper.selectById(id));
        verify(deviceShadowService).deleteByDeviceId(id);
    }

    @Test
    public void testDeleteDevice_shadowDeleteFailed() {
        // mock 数据
        IotDeviceDO dbDevice = buildDevice();
        deviceMapper.insert(dbDevice);
        // 准备参数
        Long id = dbDevice.getId();
        // mock 影子删除失败
        RuntimeException failure = new RuntimeException("shadow delete failed");
        doThrow(failure).when(deviceShadowService).deleteByDeviceId(id);

        // 调用，并断言异常
        assertSame(failure, assertThrows(RuntimeException.class, () -> deviceService.deleteDevice(id)));
        // 断言：设备删除已回滚
        assertNotNull(deviceMapper.selectById(id));
    }

    private static IotDeviceDO buildDevice() {
        return IotDeviceDO.builder().deviceName("testDevice").productId(10L).productKey("testProduct")
                .deviceType(IotProductDeviceTypeEnum.DIRECT.getType()).build();
    }

}
