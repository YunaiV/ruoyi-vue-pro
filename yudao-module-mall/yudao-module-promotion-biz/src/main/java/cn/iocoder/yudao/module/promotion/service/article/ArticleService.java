package cn.iocoder.yudao.module.promotion.service.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticleCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticleExportReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticlePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticleUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.article.ArticleDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 文章管理 Service 接口
 *
 * @author HUIHUI
 */
public interface ArticleService {

    /**
     * 创建文章管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArticle(@Valid ArticleCreateReqVO createReqVO);

    /**
     * 更新文章管理
     *
     * @param updateReqVO 更新信息
     */
    void updateArticle(@Valid ArticleUpdateReqVO updateReqVO);

    /**
     * 删除文章管理
     *
     * @param id 编号
     */
    void deleteArticle(Long id);

    /**
     * 获得文章管理
     *
     * @param id 编号
     * @return 文章管理
     */
    ArticleDO getArticle(Long id);

    /**
     * 获得文章管理列表
     *
     * @param ids 编号
     * @return 文章管理列表
     */
    List<ArticleDO> getArticleList(Collection<Long> ids);

    /**
     * 获得文章管理分页
     *
     * @param pageReqVO 分页查询
     * @return 文章管理分页
     */
    PageResult<ArticleDO> getArticlePage(ArticlePageReqVO pageReqVO);

    /**
     * 获得文章管理列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 文章管理列表
     */
    List<ArticleDO> getArticleList(ArticleExportReqVO exportReqVO);

}
