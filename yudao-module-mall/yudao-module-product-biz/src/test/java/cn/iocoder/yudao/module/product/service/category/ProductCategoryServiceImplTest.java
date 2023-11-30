package cn.iocoder.yudao.module.product.service.category;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryListReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.mysql.category.ProductCategoryMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO.PARENT_ID_NULL;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.CATEGORY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ProductCategoryServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(ProductCategoryServiceImpl.class)
public class ProductCategoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductCategoryServiceImpl productCategoryService;

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Test
    public void testCreateCategory_success() {
        // 准备参数
        ProductCategoryCreateReqVO reqVO = randomPojo(ProductCategoryCreateReqVO.class);

        // mock 父类
        ProductCategoryDO parentProductCategory = randomPojo(ProductCategoryDO.class, o -> {
            reqVO.setParentId(o.getId());
            o.setParentId(PARENT_ID_NULL);
        });
        productCategoryMapper.insert(parentProductCategory);

        // 调用
        Long categoryId = productCategoryService.createCategory(reqVO);
        // 断言
        assertNotNull(categoryId);
        // 校验记录的属性是否正确
        ProductCategoryDO category = productCategoryMapper.selectById(categoryId);
        assertPojoEquals(reqVO, category);
    }

    @Test
    public void testUpdateCategory_success() {
        // mock 数据
        ProductCategoryDO dbCategory = randomPojo(ProductCategoryDO.class);
        productCategoryMapper.insert(dbCategory);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProductCategoryUpdateReqVO reqVO = randomPojo(ProductCategoryUpdateReqVO.class, o -> {
            o.setId(dbCategory.getId()); // 设置更新的 ID
        });
        // mock 父类
        ProductCategoryDO parentProductCategory = randomPojo(ProductCategoryDO.class, o -> o.setId(reqVO.getParentId()));
        productCategoryMapper.insert(parentProductCategory);

        // 调用
        productCategoryService.updateCategory(reqVO);
        // 校验是否更新正确
        ProductCategoryDO category = productCategoryMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, category);
    }

    @Test
    public void testUpdateCategory_notExists() {
        // 准备参数
        ProductCategoryUpdateReqVO reqVO = randomPojo(ProductCategoryUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> productCategoryService.updateCategory(reqVO), CATEGORY_NOT_EXISTS);
    }

    @Test
    public void testDeleteCategory_success() {
        // mock 数据
        ProductCategoryDO dbCategory = randomPojo(ProductCategoryDO.class);
        productCategoryMapper.insert(dbCategory);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCategory.getId();

        // 调用
        productCategoryService.deleteCategory(id);
        // 校验数据不存在了
        assertNull(productCategoryMapper.selectById(id));
    }

    @Test
    public void testDeleteCategory_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> productCategoryService.deleteCategory(id), CATEGORY_NOT_EXISTS);
    }

    @Test
    public void testGetCategoryLevel() {
        // mock 数据
        ProductCategoryDO category1 = randomPojo(ProductCategoryDO.class,
                o -> o.setParentId(PARENT_ID_NULL));
        productCategoryMapper.insert(category1);
        ProductCategoryDO category2 = randomPojo(ProductCategoryDO.class,
                o -> o.setParentId(category1.getId()));
        productCategoryMapper.insert(category2);
        ProductCategoryDO category3 = randomPojo(ProductCategoryDO.class,
                o -> o.setParentId(category2.getId()));
        productCategoryMapper.insert(category3);

        // 调用，并断言
        assertEquals(productCategoryService.getCategoryLevel(category1.getId()), 1);
        assertEquals(productCategoryService.getCategoryLevel(category2.getId()), 2);
        assertEquals(productCategoryService.getCategoryLevel(category3.getId()), 3);
    }

    @Test
    public void testGetCategoryList() {
        // mock 数据
        ProductCategoryDO dbCategory = randomPojo(ProductCategoryDO.class, o -> { // 等会查询到
            o.setName("奥特曼");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setParentId(PARENT_ID_NULL);
        });
        productCategoryMapper.insert(dbCategory);
        // 测试 name 不匹配
        productCategoryMapper.insert(cloneIgnoreId(dbCategory, o -> o.setName("奥特块")));
        // 测试 status 不匹配
        productCategoryMapper.insert(cloneIgnoreId(dbCategory, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 parentId 不匹配
        productCategoryMapper.insert(cloneIgnoreId(dbCategory, o -> o.setParentId(3333L)));
        // 准备参数
        ProductCategoryListReqVO reqVO = new ProductCategoryListReqVO();
        reqVO.setName("特曼");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setParentId(PARENT_ID_NULL);

        // 调用
        List<ProductCategoryDO> list = productCategoryService.getEnableCategoryList(reqVO);
        List<ProductCategoryDO> all = productCategoryService.getEnableCategoryList(new ProductCategoryListReqVO());
        // 断言
        assertEquals(1, list.size());
        assertEquals(4, all.size());
        assertPojoEquals(dbCategory, list.get(0));
    }

}
