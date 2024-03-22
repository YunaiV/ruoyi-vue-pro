package cn.iocoder.yudao.module.bpm.service.category;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.category.BpmCategoryPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.category.BpmCategorySaveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.category.BpmCategoryMapper;
import cn.iocoder.yudao.module.bpm.service.definition.BpmCategoryServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.CATEGORY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BpmCategoryServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(BpmCategoryServiceImpl.class)
public class BpmCategoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BpmCategoryServiceImpl categoryService;

    @Resource
    private BpmCategoryMapper categoryMapper;

    @Test
    public void testCreateCategory_success() {
        // 准备参数
        BpmCategorySaveReqVO createReqVO = randomPojo(BpmCategorySaveReqVO.class).setId(null)
                .setStatus(randomCommonStatus());

        // 调用
        Long categoryId = categoryService.createCategory(createReqVO);
        // 断言
        assertNotNull(categoryId);
        // 校验记录的属性是否正确
        BpmCategoryDO category = categoryMapper.selectById(categoryId);
        assertPojoEquals(createReqVO, category, "id");
    }

    @Test
    public void testUpdateCategory_success() {
        // mock 数据
        BpmCategoryDO dbCategory = randomPojo(BpmCategoryDO.class);
        categoryMapper.insert(dbCategory);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BpmCategorySaveReqVO updateReqVO = randomPojo(BpmCategorySaveReqVO.class, o -> {
            o.setId(dbCategory.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });

        // 调用
        categoryService.updateCategory(updateReqVO);
        // 校验是否更新正确
        BpmCategoryDO category = categoryMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, category);
    }

    @Test
    public void testUpdateCategory_notExists() {
        // 准备参数
        BpmCategorySaveReqVO updateReqVO = randomPojo(BpmCategorySaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> categoryService.updateCategory(updateReqVO), CATEGORY_NOT_EXISTS);
    }

    @Test
    public void testDeleteCategory_success() {
        // mock 数据
        BpmCategoryDO dbCategory = randomPojo(BpmCategoryDO.class);
        categoryMapper.insert(dbCategory);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCategory.getId();

        // 调用
        categoryService.deleteCategory(id);
       // 校验数据不存在了
       assertNull(categoryMapper.selectById(id));
    }

    @Test
    public void testDeleteCategory_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> categoryService.deleteCategory(id), CATEGORY_NOT_EXISTS);
    }

    @Test
    public void testGetCategoryPage() {
       // mock 数据
       BpmCategoryDO dbCategory = randomPojo(BpmCategoryDO.class, o -> { // 等会查询到
           o.setName("芋头");
           o.setCode("xiaodun");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setCreateTime(buildTime(2023, 2, 2));
       });
       categoryMapper.insert(dbCategory);
       // 测试 name 不匹配
       categoryMapper.insert(cloneIgnoreId(dbCategory, o -> o.setName("小盾")));
       // 测试 code 不匹配
       categoryMapper.insert(cloneIgnoreId(dbCategory, o -> o.setCode("tudou")));
       // 测试 status 不匹配
       categoryMapper.insert(cloneIgnoreId(dbCategory, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 createTime 不匹配
       categoryMapper.insert(cloneIgnoreId(dbCategory, o -> o.setCreateTime(buildTime(2024, 2, 2))));
       // 准备参数
       BpmCategoryPageReqVO reqVO = new BpmCategoryPageReqVO();
       reqVO.setName("芋");
       reqVO.setCode("xiao");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<BpmCategoryDO> pageResult = categoryService.getCategoryPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCategory, pageResult.getList().get(0));
    }

}