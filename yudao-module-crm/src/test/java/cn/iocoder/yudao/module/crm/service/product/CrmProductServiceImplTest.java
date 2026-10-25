package cn.iocoder.yudao.module.crm.service.product;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.dal.mysql.product.CrmProductMapper;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * {@link CrmProductServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class CrmProductServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private CrmProductServiceImpl productService;

    @Mock
    private CrmProductMapper productMapper;
    @Mock
    private CrmProductCategoryService productCategoryService;
    @Mock
    private CrmContractService contractService;
    @Mock
    private CrmBusinessService businessService;
    @Mock
    private CrmPermissionService permissionService;
    @Mock
    private AdminUserApi adminUserApi;

    @BeforeEach
    public void setUp() {
        LogRecordContext.putEmptySpan();
    }

    @AfterEach
    public void tearDown() {
        LogRecordContext.clear();
    }

    @Test
    public void testDeleteProduct_success() {
        // mock 数据
        CrmProductDO product = new CrmProductDO().setId(1L).setName("芋道产品");
        when(productMapper.selectById(1L)).thenReturn(product);
        // mock 方法（未被合同、商机引用）
        when(contractService.getContractProductCountByProductId(1L)).thenReturn(0L);
        when(businessService.getBusinessProductCountByProductId(1L)).thenReturn(0L);

        // 调用
        productService.deleteProduct(1L);
        // 断言
        verify(productMapper).deleteById(1L);
        // 断言操作日志上下文，为删除前的产品
        CrmProductDO logProduct = (CrmProductDO) LogRecordContext.getVariable("product");
        assertSame(product, logProduct);
        assertEquals("芋道产品", logProduct.getName());
    }

    @Test
    public void testDeleteProduct_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(1L), PRODUCT_NOT_EXISTS);
        // 断言
        verify(productMapper, never()).deleteById(anyLong());
        assertNull(LogRecordContext.getVariable("product"));
    }

    @Test
    public void testDeleteProduct_contractExists() {
        // mock 数据
        when(productMapper.selectById(1L)).thenReturn(new CrmProductDO().setId(1L).setName("芋道产品"));
        // mock 方法（被合同引用）
        when(contractService.getContractProductCountByProductId(1L)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(1L), PRODUCT_DELETE_FAIL_CONTRACT_EXISTS);
        // 断言
        verify(productMapper, never()).deleteById(anyLong());
        assertNull(LogRecordContext.getVariable("product"));
    }

    @Test
    public void testDeleteProduct_businessExists() {
        // mock 数据
        when(productMapper.selectById(1L)).thenReturn(new CrmProductDO().setId(1L).setName("芋道产品"));
        // mock 方法（被商机引用）
        when(contractService.getContractProductCountByProductId(1L)).thenReturn(0L);
        when(businessService.getBusinessProductCountByProductId(1L)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(1L), PRODUCT_DELETE_FAIL_BUSINESS_EXISTS);
        // 断言
        verify(productMapper, never()).deleteById(anyLong());
        assertNull(LogRecordContext.getVariable("product"));
    }

}
