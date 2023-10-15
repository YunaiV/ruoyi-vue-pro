package cn.iocoder.yudao.module.promotion.service.article;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticleCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticleExportReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticlePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticleUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.article.vo.article.AppArticlePageReqVO;
import cn.iocoder.yudao.module.promotion.convert.article.ArticleConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.article.ArticleDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.article.ArticleMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.ARTICLE_NOT_EXISTS;

/**
 * 文章管理 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public Long createArticle(ArticleCreateReqVO createReqVO) {
        // 插入
        ArticleDO article = ArticleConvert.INSTANCE.convert(createReqVO);
        articleMapper.insert(article);
        // 返回
        return article.getId();
    }

    @Override
    public void updateArticle(ArticleUpdateReqVO updateReqVO) {
        // 校验存在
        validateArticleExists(updateReqVO.getId());
        // 更新
        ArticleDO updateObj = ArticleConvert.INSTANCE.convert(updateReqVO);
        articleMapper.updateById(updateObj);
    }

    @Override
    public void deleteArticle(Long id) {
        // 校验存在
        validateArticleExists(id);
        // 删除
        articleMapper.deleteById(id);
    }

    private void validateArticleExists(Long id) {
        if (articleMapper.selectById(id) == null) {
            throw exception(ARTICLE_NOT_EXISTS);
        }
    }

    @Override
    public ArticleDO getArticle(Long id) {
        return articleMapper.selectById(id);
    }

    @Override
    public List<ArticleDO> getArticleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return articleMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ArticleDO> getArticlePage(ArticlePageReqVO pageReqVO) {
        return articleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ArticleDO> getArticleList(ArticleExportReqVO exportReqVO) {
        return articleMapper.selectList(exportReqVO);
    }

    @Override
    public List<ArticleDO> getArticleCategoryListByRecommendHotAndRecommendBanner(Boolean recommendHot, Boolean recommendBanner) {
        return articleMapper.selectList(recommendHot, recommendBanner);
    }

    @Override
    public PageResult<ArticleDO> getArticlePage(AppArticlePageReqVO pageReqVO) {
        return articleMapper.selectPage(pageReqVO);
    }

}
