package cn.iocoder.yudao.module.promotion.service.articlecategory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo.ArticleCategoryCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo.ArticleCategoryExportReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo.ArticleCategoryPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo.ArticleCategoryUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.articlecategory.ArticleCategoryConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.articlecategory.ArticleCategoryDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.articlecategory.ArticleCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.ARTICLE_CATEGORY_NOT_EXISTS;

/**
 * 文章分类 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class ArticleCategoryServiceImpl implements ArticleCategoryService {

    @Resource
    private ArticleCategoryMapper articleCategoryMapper;

    @Override
    public Long createArticleCategory(ArticleCategoryCreateReqVO createReqVO) {
        // 插入
        ArticleCategoryDO articleCategory = ArticleCategoryConvert.INSTANCE.convert(createReqVO);
        articleCategoryMapper.insert(articleCategory);
        // 返回
        return articleCategory.getId();
    }

    @Override
    public void updateArticleCategory(ArticleCategoryUpdateReqVO updateReqVO) {
        // 校验存在
        validateArticleCategoryExists(updateReqVO.getId());
        // 更新
        ArticleCategoryDO updateObj = ArticleCategoryConvert.INSTANCE.convert(updateReqVO);
        articleCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteArticleCategory(Long id) {
        // 校验存在
        validateArticleCategoryExists(id);
        // 删除
        articleCategoryMapper.deleteById(id);
    }

    private void validateArticleCategoryExists(Long id) {
        if (articleCategoryMapper.selectById(id) == null) {
            throw exception(ARTICLE_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public ArticleCategoryDO getArticleCategory(Long id) {
        return articleCategoryMapper.selectById(id);
    }

    @Override
    public List<ArticleCategoryDO> getArticleCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return articleCategoryMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ArticleCategoryDO> getArticleCategoryPage(ArticleCategoryPageReqVO pageReqVO) {
        return articleCategoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ArticleCategoryDO> getArticleCategoryList(ArticleCategoryExportReqVO exportReqVO) {
        return articleCategoryMapper.selectList(exportReqVO);
    }

    @Override
    public List<ArticleCategoryDO> getArticleCategoryListByStatus(Integer status) {
        return articleCategoryMapper.selectList(ArticleCategoryDO::getStatus, status);
    }

}
