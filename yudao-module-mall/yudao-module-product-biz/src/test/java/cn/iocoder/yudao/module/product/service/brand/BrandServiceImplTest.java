package cn.iocoder.yudao.module.product.service.brand;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.product.controller.admin.brand.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.BrandDO;
import cn.iocoder.yudao.module.product.dal.mysql.brand.BrandMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
* {@link BrandServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(BrandServiceImpl.class)
public class BrandServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BrandServiceImpl brandService;

    @Resource
    private BrandMapper brandMapper;

    @Test
    public void testCreateBrand_success() {
        // 准备参数
        BrandCreateReqVO reqVO = randomPojo(BrandCreateReqVO.class);

        // 调用
        Long brandId = brandService.createBrand(reqVO);
        // 断言
        assertNotNull(brandId);
        // 校验记录的属性是否正确
        BrandDO brand = brandMapper.selectById(brandId);
        assertPojoEquals(reqVO, brand);
    }

    @Test
    public void testUpdateBrand_success() {
        // mock 数据
        BrandDO dbBrand = randomPojo(BrandDO.class);
        brandMapper.insert(dbBrand);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BrandUpdateReqVO reqVO = randomPojo(BrandUpdateReqVO.class, o -> {
            o.setId(dbBrand.getId()); // 设置更新的 ID
        });

        // 调用
        brandService.updateBrand(reqVO);
        // 校验是否更新正确
        BrandDO brand = brandMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, brand);
    }

    @Test
    public void testUpdateBrand_notExists() {
        // 准备参数
        BrandUpdateReqVO reqVO = randomPojo(BrandUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> brandService.updateBrand(reqVO), BRAND_NOT_EXISTS);
    }

    @Test
    public void testDeleteBrand_success() {
        // mock 数据
        BrandDO dbBrand = randomPojo(BrandDO.class);
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
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBrandPage() {
       // mock 数据
       BrandDO dbBrand = randomPojo(BrandDO.class, o -> { // 等会查询到
           o.setCategoryId(null);
           o.setName(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       brandMapper.insert(dbBrand);
       // 测试 categoryId 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setCategoryId(null)));
       // 测试 name 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setName(null)));
       // 测试 status 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setCreateTime(null)));
       // 准备参数
       BrandPageReqVO reqVO = new BrandPageReqVO();
       reqVO.setCategoryId(null);
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<BrandDO> pageResult = brandService.getBrandPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbBrand, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBrandList() {
       // mock 数据
       BrandDO dbBrand = randomPojo(BrandDO.class, o -> { // 等会查询到
           o.setCategoryId(null);
           o.setName(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       brandMapper.insert(dbBrand);
       // 测试 categoryId 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setCategoryId(null)));
       // 测试 name 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setName(null)));
       // 测试 status 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       brandMapper.insert(cloneIgnoreId(dbBrand, o -> o.setCreateTime(null)));
       // 准备参数
       BrandExportReqVO reqVO = new BrandExportReqVO();
       reqVO.setCategoryId(null);
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       List<BrandDO> list = brandService.getBrandList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbBrand, list.get(0));
    }

}
