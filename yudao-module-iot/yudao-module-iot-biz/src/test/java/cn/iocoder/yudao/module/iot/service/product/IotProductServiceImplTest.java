package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.Serializable;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotProductServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class IotProductServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotProductServiceImpl productService;

    @Mock
    private IotProductMapper productMapper;
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDevicePropertyService devicePropertyDataService;

    @Test
    public void testDeleteProduct_success() {
        // 准备参数
        Long id = 1L;
        // mock 方法
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(0L);

        // 调用
        productService.deleteProduct(id);
        // 断言：先删除属性表，再删除 MySQL 产品
        InOrder inOrder = inOrder(devicePropertyDataService, productMapper);
        inOrder.verify(devicePropertyDataService).deleteDevicePropertyData(id);
        inOrder.verify(productMapper).deleteById(id);
    }

    @Test
    public void testDeleteProduct_notExists() {
        // 准备参数
        Long id = 1L;

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_NOT_EXISTS);
        // 断言：不删除属性表、产品
        assertNoDelete();
    }

    @Test
    public void testDeleteProduct_published() {
        // 准备参数
        Long id = 1L;
        // mock 方法
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.PUBLISHED.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_STATUS_NOT_DELETE);
        // 断言：不删除属性表、产品
        assertNoDelete();
        verifyNoInteractions(deviceService);
    }

    @Test
    public void testDeleteProduct_hasDevice() {
        // 准备参数
        Long id = 1L;
        // mock 方法
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_DELETE_FAIL_HAS_DEVICE);
        // 断言：不删除属性表、产品
        assertNoDelete();
    }

    @Test
    public void testDeleteProduct_deletePropertyTableFailed() {
        // 准备参数
        Long id = 1L;
        // mock 方法：属性表删除失败
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(0L);
        RuntimeException failure = new RuntimeException("drop property table failed");
        doThrow(failure).when(devicePropertyDataService).deleteDevicePropertyData(id);

        // 调用，并断言异常
        assertSame(failure, assertThrows(RuntimeException.class, () -> productService.deleteProduct(id)));
        // 断言：保留 MySQL 产品，便于重试
        verify(productMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    public void testDeleteProduct_deleteProductFailedThenRetry() {
        // 准备参数
        Long id = 1L;
        // mock 方法：第一次 MySQL 删除失败，第二次成功
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(0L);
        RuntimeException failure = new RuntimeException("delete product failed");
        when(productMapper.deleteById(id)).thenThrow(failure).thenReturn(1);

        // 调用：第一次失败
        assertSame(failure, assertThrows(RuntimeException.class, () -> productService.deleteProduct(id)));
        // 调用：第二次重试成功
        productService.deleteProduct(id);
        // 断言：属性表删除是幂等的，重试时再次调用
        verify(devicePropertyDataService, times(2)).deleteDevicePropertyData(id);
        verify(productMapper, times(2)).deleteById(id);
    }

    private void assertNoDelete() {
        verifyNoInteractions(devicePropertyDataService);
        verify(productMapper, never()).deleteById(any(Serializable.class));
    }

    private static IotProductDO buildProduct(Long id, Integer status) {
        return IotProductDO.builder().id(id).status(status).build();
    }

}
