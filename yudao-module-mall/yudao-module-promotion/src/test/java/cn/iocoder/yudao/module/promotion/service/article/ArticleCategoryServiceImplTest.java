package cn.iocoder.yudao.module.promotion.service.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.category.ArticleCategoryCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.category.ArticleCategoryPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.category.ArticleCategoryUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.article.ArticleCategoryDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.article.ArticleCategoryMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.ARTICLE_CATEGORY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：review 单测
/**
 * {@link ArticleCategoryServiceImpl} 的单元测试类
 *
 * @author HUIHUI
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(ArticleCategoryServiceImpl.class)
public class ArticleCategoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ArticleCategoryServiceImpl articleCategoryService;

    @Resource
    private ArticleCategoryMapper articleCategoryMapper;

    @Test
    public void testCreateArticleCategory_success() {
        // 准备参数
        ArticleCategoryCreateReqVO reqVO = randomPojo(ArticleCategoryCreateReqVO.class);

        // 调用
        Long articleCategoryId = articleCategoryService.createArticleCategory(reqVO);
        // 断言
        assertNotNull(articleCategoryId);
        // 校验记录的属性是否正确
        ArticleCategoryDO articleCategory = articleCategoryMapper.selectById(articleCategoryId);
        assertPojoEquals(reqVO, articleCategory);
    }

    @Test
    public void testUpdateArticleCategory_success() {
        // mock 数据
        ArticleCategoryDO dbArticleCategory = randomPojo(ArticleCategoryDO.class);
        articleCategoryMapper.insert(dbArticleCategory);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ArticleCategoryUpdateReqVO reqVO = randomPojo(ArticleCategoryUpdateReqVO.class, o -> {
            o.setId(dbArticleCategory.getId()); // 设置更新的 ID
        });

        // 调用
        articleCategoryService.updateArticleCategory(reqVO);
        // 校验是否更新正确
        ArticleCategoryDO articleCategory = articleCategoryMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, articleCategory);
    }

    @Test
    public void testUpdateArticleCategory_notExists() {
        // 准备参数
        ArticleCategoryUpdateReqVO reqVO = randomPojo(ArticleCategoryUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> articleCategoryService.updateArticleCategory(reqVO), ARTICLE_CATEGORY_NOT_EXISTS);
    }

    @Test
    public void testDeleteArticleCategory_success() {
        // mock 数据
        ArticleCategoryDO dbArticleCategory = randomPojo(ArticleCategoryDO.class);
        articleCategoryMapper.insert(dbArticleCategory);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbArticleCategory.getId();

        // 调用
        articleCategoryService.deleteArticleCategory(id);
        // 校验数据不存在了
        assertNull(articleCategoryMapper.selectById(id));
    }

    @Test
    public void testDeleteArticleCategory_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> articleCategoryService.deleteArticleCategory(id), ARTICLE_CATEGORY_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetArticleCategoryPage() {
        // mock 数据
        ArticleCategoryDO dbArticleCategory = randomPojo(ArticleCategoryDO.class, o -> { // 等会查询到
            o.setName(null);
            o.setPicUrl(null);
            o.setStatus(null);
            o.setSort(null);
            o.setCreateTime(null);
        });
        articleCategoryMapper.insert(dbArticleCategory);
        // 测试 name 不匹配
        articleCategoryMapper.insert(cloneIgnoreId(dbArticleCategory, o -> o.setName(null)));
        // 测试 picUrl 不匹配
        articleCategoryMapper.insert(cloneIgnoreId(dbArticleCategory, o -> o.setPicUrl(null)));
        // 测试 status 不匹配
        articleCategoryMapper.insert(cloneIgnoreId(dbArticleCategory, o -> o.setStatus(null)));
        // 测试 sort 不匹配
        articleCategoryMapper.insert(cloneIgnoreId(dbArticleCategory, o -> o.setSort(null)));
        // 测试 createTime 不匹配
        articleCategoryMapper.insert(cloneIgnoreId(dbArticleCategory, o -> o.setCreateTime(null)));
        // 准备参数
        ArticleCategoryPageReqVO reqVO = new ArticleCategoryPageReqVO();
        reqVO.setName(null);
        reqVO.setStatus(null);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<ArticleCategoryDO> pageResult = articleCategoryService.getArticleCategoryPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbArticleCategory, pageResult.getList().get(0));
    }

}
