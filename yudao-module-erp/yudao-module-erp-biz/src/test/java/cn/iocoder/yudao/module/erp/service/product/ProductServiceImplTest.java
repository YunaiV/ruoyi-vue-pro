package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.erp.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ErpProductServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(ErpProductServiceImpl.class)
public class ProductServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ErpProductServiceImpl productService;

    @Resource
    private ErpProductMapper productMapper;

    @Test
    public void testCreateProduct_success() {
        // 准备参数
        ProductSaveReqVO createReqVO = randomPojo(ProductSaveReqVO.class).setId(null);

        // 调用
        Long productId = productService.createProduct(createReqVO);
        // 断言
        assertNotNull(productId);
        // 校验记录的属性是否正确
        ErpProductDO product = productMapper.selectById(productId);
        assertPojoEquals(createReqVO, product, "id");
    }

    @Test
    public void testUpdateProduct_success() {
        // mock 数据
        ErpProductDO dbProduct = randomPojo(ErpProductDO.class);
        productMapper.insert(dbProduct);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProductSaveReqVO updateReqVO = randomPojo(ProductSaveReqVO.class, o -> {
            o.setId(dbProduct.getId()); // 设置更新的 ID
        });

        // 调用
        productService.updateProduct(updateReqVO);
        // 校验是否更新正确
        ErpProductDO product = productMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, product);
    }

    @Test
    public void testUpdateProduct_notExists() {
        // 准备参数
        ProductSaveReqVO updateReqVO = randomPojo(ProductSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> productService.updateProduct(updateReqVO), PRODUCT_NOT_EXISTS);
    }

    @Test
    public void testDeleteProduct_success() {
        // mock 数据
        ErpProductDO dbProduct = randomPojo(ErpProductDO.class);
        productMapper.insert(dbProduct);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProduct.getId();

        // 调用
        productService.deleteProduct(id);
       // 校验数据不存在了
       assertNull(productMapper.selectById(id));
    }

    @Test
    public void testDeleteProduct_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProductPage() {
       // mock 数据
       ErpProductDO dbProduct = randomPojo(ErpProductDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setCategoryId(null);
           o.setCreateTime(null);
       });
       productMapper.insert(dbProduct);
       // 测试 name 不匹配
       productMapper.insert(cloneIgnoreId(dbProduct, o -> o.setName(null)));
       // 测试 categoryId 不匹配
       productMapper.insert(cloneIgnoreId(dbProduct, o -> o.setCategoryId(null)));
       // 测试 createTime 不匹配
       productMapper.insert(cloneIgnoreId(dbProduct, o -> o.setCreateTime(null)));
       // 准备参数
       ProductPageReqVO reqVO = new ProductPageReqVO();
       reqVO.setName(null);
       reqVO.setCategoryId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ErpProductDO> pageResult = productService.getProductPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProduct, pageResult.getList().get(0));
    }

}