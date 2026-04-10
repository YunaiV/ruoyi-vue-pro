package cn.iocoder.yudao.module.promotion.service.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticleCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticlePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticleUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.article.ArticleDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.article.ArticleMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.ARTICLE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ArticleServiceImpl} 的单元测试类
 *
 * @author HUIHUI
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(ArticleServiceImpl.class)
public class ArticleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ArticleServiceImpl articleService;

    @Resource
    private ArticleMapper articleMapper;

    @Test
    public void testCreateArticle_success() {
        // 准备参数
        ArticleCreateReqVO reqVO = randomPojo(ArticleCreateReqVO.class);

        // 调用
        Long articleId = articleService.createArticle(reqVO);
        // 断言
        assertNotNull(articleId);
        // 校验记录的属性是否正确
        ArticleDO article = articleMapper.selectById(articleId);
        assertPojoEquals(reqVO, article);
    }

    @Test
    public void testUpdateArticle_success() {
        // mock 数据
        ArticleDO dbArticle = randomPojo(ArticleDO.class);
        articleMapper.insert(dbArticle);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ArticleUpdateReqVO reqVO = randomPojo(ArticleUpdateReqVO.class, o -> {
            o.setId(dbArticle.getId()); // 设置更新的 ID
        });

        // 调用
        articleService.updateArticle(reqVO);
        // 校验是否更新正确
        ArticleDO article = articleMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, article);
    }

    @Test
    public void testUpdateArticle_notExists() {
        // 准备参数
        ArticleUpdateReqVO reqVO = randomPojo(ArticleUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> articleService.updateArticle(reqVO), ARTICLE_NOT_EXISTS);
    }

    @Test
    public void testDeleteArticle_success() {
        // mock 数据
        ArticleDO dbArticle = randomPojo(ArticleDO.class);
        articleMapper.insert(dbArticle);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbArticle.getId();

        // 调用
        articleService.deleteArticle(id);
        // 校验数据不存在了
        assertNull(articleMapper.selectById(id));
    }

    @Test
    public void testDeleteArticle_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> articleService.deleteArticle(id), ARTICLE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetArticlePage() {
        // mock 数据
        ArticleDO dbArticle = randomPojo(ArticleDO.class, o -> { // 等会查询到
            o.setCategoryId(null);
            o.setTitle(null);
            o.setAuthor(null);
            o.setPicUrl(null);
            o.setIntroduction(null);
            o.setBrowseCount(null);
            o.setSort(null);
            o.setStatus(null);
            o.setSpuId(null);
            o.setRecommendHot(null);
            o.setRecommendBanner(null);
            o.setContent(null);
            o.setCreateTime(null);
        });
        articleMapper.insert(dbArticle);
        // 测试 categoryId 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setCategoryId(null)));
        // 测试 title 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setTitle(null)));
        // 测试 author 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setAuthor(null)));
        // 测试 picUrl 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setPicUrl(null)));
        // 测试 introduction 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setIntroduction(null)));
        // 测试 browseCount 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setBrowseCount(null)));
        // 测试 sort 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setSort(null)));
        // 测试 status 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setStatus(null)));
        // 测试 spuId 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setSpuId(null)));
        // 测试 recommendHot 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setRecommendHot(null)));
        // 测试 recommendBanner 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setRecommendBanner(null)));
        // 测试 content 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setContent(null)));
        // 测试 createTime 不匹配
        articleMapper.insert(cloneIgnoreId(dbArticle, o -> o.setCreateTime(null)));
        // 准备参数
        ArticlePageReqVO reqVO = new ArticlePageReqVO();
        reqVO.setCategoryId(null);
        reqVO.setTitle(null);
        reqVO.setAuthor(null);
        reqVO.setStatus(null);
        reqVO.setSpuId(null);
        reqVO.setRecommendHot(null);
        reqVO.setRecommendBanner(null);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<ArticleDO> pageResult = articleService.getArticlePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbArticle, pageResult.getList().get(0));
    }

}
