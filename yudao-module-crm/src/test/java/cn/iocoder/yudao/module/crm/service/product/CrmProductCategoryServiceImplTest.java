package cn.iocoder.yudao.module.crm.service.product;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.mysql.product.CrmProductCategoryMapper;
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
 * {@link CrmProductCategoryServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class CrmProductCategoryServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private CrmProductCategoryServiceImpl productCategoryService;

    @Mock
    private CrmProductCategoryMapper productCategoryMapper;
    @Mock
    private CrmProductService crmProductService;

    @BeforeEach
    public void setUp() {
        LogRecordContext.putEmptySpan();
    }

    @AfterEach
    public void tearDown() {
        LogRecordContext.clear();
    }

    @Test
    public void testDeleteProductCategory_success() {
        // mock 数据
        CrmProductCategoryDO category = new CrmProductCategoryDO().setId(1L).setName("芋道分类").setParentId(0L);
        when(productCategoryMapper.selectById(1L)).thenReturn(category);
        // mock 方法（无子分类、未被产品使用）
        when(productCategoryMapper.selectCountByParentId(1L)).thenReturn(0L);
        when(crmProductService.getProductByCategoryId(1L)).thenReturn(0L);

        // 调用
        productCategoryService.deleteProductCategory(1L);
        // 断言
        verify(productCategoryMapper).deleteById(1L);
        // 断言操作日志上下文，为删除前的分类
        CrmProductCategoryDO logCategory = (CrmProductCategoryDO) LogRecordContext.getVariable("productCategory");
        assertSame(category, logCategory);
        assertEquals("芋道分类", logCategory.getName());
    }

    @Test
    public void testDeleteProductCategory_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> productCategoryService.deleteProductCategory(1L), PRODUCT_CATEGORY_NOT_EXISTS);
        // 断言
        verify(productCategoryMapper, never()).deleteById(anyLong());
        assertNull(LogRecordContext.getVariable("productCategory"));
    }

    @Test
    public void testDeleteProductCategory_existsChildren() {
        // mock 数据
        when(productCategoryMapper.selectById(1L)).thenReturn(new CrmProductCategoryDO().setId(1L).setName("芋道分类"));
        // mock 方法（存在子分类）
        when(productCategoryMapper.selectCountByParentId(1L)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> productCategoryService.deleteProductCategory(1L), PRODUCT_CATEGORY_EXISTS_CHILDREN);
        // 断言
        verify(productCategoryMapper, never()).deleteById(anyLong());
        verifyNoInteractions(crmProductService);
        assertNull(LogRecordContext.getVariable("productCategory"));
    }

    @Test
    public void testDeleteProductCategory_used() {
        // mock 数据
        when(productCategoryMapper.selectById(1L)).thenReturn(new CrmProductCategoryDO().setId(1L).setName("芋道分类"));
        // mock 方法（被产品使用）
        when(productCategoryMapper.selectCountByParentId(1L)).thenReturn(0L);
        when(crmProductService.getProductByCategoryId(1L)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> productCategoryService.deleteProductCategory(1L), PRODUCT_CATEGORY_USED);
        // 断言
        verify(productCategoryMapper, never()).deleteById(anyLong());
        assertNull(LogRecordContext.getVariable("productCategory"));
    }

}
