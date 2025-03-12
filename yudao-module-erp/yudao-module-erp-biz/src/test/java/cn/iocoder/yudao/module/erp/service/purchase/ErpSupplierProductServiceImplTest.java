package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierProductMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
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
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_PRODUCT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ErpSupplierProductServiceImpl} 的单元测试类
 *
 * @author 索迈管理员
 */
@Import(ErpSupplierProductServiceImpl.class)
public class ErpSupplierProductServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ErpSupplierProductServiceImpl supplierProductService;

    @Resource
    private ErpSupplierProductMapper supplierProductMapper;

    @Resource
    private ErpNoRedisDAO erpNoRedisDAO;

    @Test
    public void testCreateSupplierProduct_success() {
        // 准备参数
        ErpSupplierProductSaveReqVO createReqVO = randomPojo(ErpSupplierProductSaveReqVO.class).setId(null);

        // 调用
        Long supplierProductId = supplierProductService.createSupplierProduct(createReqVO);
        // 断言
        assertNotNull(supplierProductId);
        // 校验记录的属性是否正确
        ErpSupplierProductDO supplierProduct = supplierProductMapper.selectById(supplierProductId);
        assertPojoEquals(createReqVO, supplierProduct, "id");
    }

    @Test
    public void testUpdateSupplierProduct_success() {
        // mock 数据
        ErpSupplierProductDO dbSupplierProduct = randomPojo(ErpSupplierProductDO.class);
        supplierProductMapper.insert(dbSupplierProduct);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ErpSupplierProductSaveReqVO updateReqVO = randomPojo(ErpSupplierProductSaveReqVO.class, o -> {
            o.setId(dbSupplierProduct.getId()); // 设置更新的 ID
        });

        // 调用
        supplierProductService.updateSupplierProduct(updateReqVO);
        // 校验是否更新正确
        ErpSupplierProductDO supplierProduct = supplierProductMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, supplierProduct);
    }

    @Test
    public void testUpdateSupplierProduct_notExists() {
        // 准备参数
        ErpSupplierProductSaveReqVO updateReqVO = randomPojo(ErpSupplierProductSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> supplierProductService.updateSupplierProduct(updateReqVO), SUPPLIER_PRODUCT_NOT_EXISTS);
    }

    @Test
    public void testDeleteSupplierProduct_success() {
        // mock 数据
        ErpSupplierProductDO dbSupplierProduct = randomPojo(ErpSupplierProductDO.class);
        supplierProductMapper.insert(dbSupplierProduct);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSupplierProduct.getId();

        // 调用
        supplierProductService.deleteSupplierProduct(id);
       // 校验数据不存在了
       assertNull(supplierProductMapper.selectById(id));
    }

    @Test
    public void testDeleteSupplierProduct_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> supplierProductService.deleteSupplierProduct(id), SUPPLIER_PRODUCT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSupplierProductPage() {
       // mock 数据
       ErpSupplierProductDO dbSupplierProduct = randomPojo(ErpSupplierProductDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setSupplierId(null);
           o.setProductId(null);
           o.setPackageHeight(null);
           o.setPackageLength(null);
           o.setPackageWeight(null);
           o.setPackageWidth(null);
           o.setPurchasePrice(null);
           o.setPurchasePriceCurrencyCode(null);
           o.setCreateTime(null);
       });
       supplierProductMapper.insert(dbSupplierProduct);
       // 测试 code 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setCode(null)));
       // 测试 supplierId 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setSupplierId(null)));
       // 测试 productId 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setProductId(null)));
       // 测试 packageHeight 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setPackageHeight(null)));
       // 测试 packageLength 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setPackageLength(null)));
       // 测试 packageWeight 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setPackageWeight(null)));
       // 测试 packageWidth 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setPackageWidth(null)));
       // 测试 purchasePrice 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setPurchasePrice(null)));
       // 测试 purchasePriceCurrencyCode 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setPurchasePriceCurrencyCode(null)));
       // 测试 createTime 不匹配
       supplierProductMapper.insert(cloneIgnoreId(dbSupplierProduct, o -> o.setCreateTime(null)));
       // 准备参数
       ErpSupplierProductPageReqVO reqVO = new ErpSupplierProductPageReqVO();
       reqVO.setCode(null);
       reqVO.setSupplierId(null);
       reqVO.setProductId(null);
       reqVO.setPackageHeight(null);
       reqVO.setPackageLength(null);
       reqVO.setPackageWeight(null);
       reqVO.setPackageWidth(null);
       reqVO.setPurchasePrice(null);
       reqVO.setPurchasePriceCurrencyCode(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ErpSupplierProductDO> pageResult = supplierProductService.getSupplierProductPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSupplierProduct, pageResult.getList().get(0));
    }

}