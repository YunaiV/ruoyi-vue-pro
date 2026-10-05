package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.product.IotProductController;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.bind.annotation.RequestParam;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link IotProductServiceImpl} 的产品表结构同步单元测试
 *
 * @author 芋道源码
 */
@Import(IotProductServiceImpl.class)
public class IotProductSyncTest extends BaseDbUnitTest {

    @Resource
    private IotProductServiceImpl productService;

    @Resource
    private IotProductMapper productMapper;

    @MockBean
    private IotDeviceService deviceService;
    @MockBean
    private IotDevicePropertyService devicePropertyDataService;

    @Test
    public void testSyncProductPropertyTable_success() {
        // mock 数据
        IotProductDO dbProduct = randomPojo(IotProductDO.class);
        productMapper.insert(dbProduct);
        IotProductDO otherProduct = randomPojo(IotProductDO.class);
        productMapper.insert(otherProduct);
        // 准备参数
        Long id = dbProduct.getId();

        // 调用
        productService.syncProductPropertyTable(id);
        // 断言：只同步指定产品
        verify(devicePropertyDataService).defineDevicePropertyData(id);
        verifyNoMoreInteractions(devicePropertyDataService);
    }

    @Test
    public void testSyncProductPropertyTable_productNotExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> productService.syncProductPropertyTable(id), PRODUCT_NOT_EXISTS);
        verifyNoInteractions(devicePropertyDataService);
    }

    @Test
    public void testSyncProductPropertyTable_syncFailed() {
        // mock 数据
        IotProductDO dbProduct = randomPojo(IotProductDO.class);
        productMapper.insert(dbProduct);
        // 准备参数
        Long id = dbProduct.getId();
        // mock 同步失败
        RuntimeException failure = new RuntimeException("TDengine sync failed");
        doThrow(failure).when(devicePropertyDataService).defineDevicePropertyData(id);

        // 调用，并断言异常
        assertSame(failure, assertThrows(RuntimeException.class, () -> productService.syncProductPropertyTable(id)));
    }

    @Test
    public void testController_syncProductPropertyTable_idRequired() throws Exception {
        // 调用：读取请求参数定义
        RequestParam parameter = IotProductController.class.getDeclaredMethod("syncProductPropertyTable", Long.class)
                .getParameters()[0].getAnnotation(RequestParam.class);
        // 断言
        assertEquals("id", parameter.value());
        assertTrue(parameter.required());
    }
}
