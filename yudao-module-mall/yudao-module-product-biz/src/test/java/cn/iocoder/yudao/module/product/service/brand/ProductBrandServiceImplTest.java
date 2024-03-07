package cn.iocoder.yudao.module.product.service.brand;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.ProductBrandCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.ProductBrandPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.ProductBrandUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.ProductBrandDO;
import cn.iocoder.yudao.module.product.dal.mysql.brand.ProductBrandMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.BRAND_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link ProductBrandServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(ProductBrandServiceImpl.class)
public class ProductBrandServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductBrandServiceImpl brandService;

    @Resource
    private ProductBrandMapper brandMapper;

    @Test
    public void testCreateBrand_success() {
        // 准备参数
        ProductBrandCreateReqVO reqVO = randomPojo(ProductBrandCreateReqVO.class);

        // 调用
        Long brandId = brandService.createBrand(reqVO);
        // 断言
        assertNotNull(brandId);
        // 校验记录的属性是否正确
        ProductBrandDO brand = brandMapper.selectById(brandId);
        assertPojoEquals(reqVO, brand);
    }

    @Test
    public void testUpdateBrand_success() {
        // mock 数据
        ProductBrandDO dbBrand = randomPojo(ProductBrandDO.class);
        brandMapper.insert(dbBrand);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProductBrandUpdateReqVO reqVO = randomPojo(ProductBrandUpdateReqVO.class, o -> {
            o.setId(dbBrand.getId()); // 设置更新的 ID
        });

        // 调用
        brandService.updateBrand(reqVO);
        // 校验是否更新正确
        ProductBrandDO brand = brandMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, brand);
    }

    @Test
    public void testUpdateBrand_notExists() {
        // 准备参数
        ProductBrandUpdateReqVO reqVO = randomPojo(ProductBrandUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> brandService.updateBrand(reqVO), BRAND_NOT_EXISTS);
    }

    @Test
    public void testDeleteBrand_success() {
        // mock 数据
        ProductBrandDO dbBrand = randomPojo(ProductBrandDO.class);
        brandMapper.insert(dbBrand);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbBrand.getId();

        // 调用
        brandService.deleteBrand(id);
       // 校验数据不存在了
       assertNull(brandMapper.selectById(id));
    }

    @Test
    public void testDeleteBrand_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> brandService.deleteBrand(id), BRAND_NOT_EXISTS);
    }

    @Test
    public void testGetBrandPage() {
       // mock 数据
       ProductBrandDO dbBrand = randomPojo(ProductBrandDO.class, o -> { // 等会查询到
           o.setName("芋道源码");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setCreateTime(buildTime(2022, 2, 1));
       });
       brandMapper.insert(dbBrand);
       // 测试 name 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setName("源码")));
       // 测试 status 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 createTime 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setCreateTime(buildTime(2022, 3, 1))));
       // 准备参数
       ProductBrandPageReqVO reqVO = new ProductBrandPageReqVO();
       reqVO.setName("芋道");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setCreateTime((new LocalDateTime[]{buildTime(2022, 1, 1), buildTime(2022, 2, 25)}));

       // 调用
       PageResult<ProductBrandDO> pageResult = brandService.getBrandPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbBrand, pageResult.getList().get(0));
    }

}
