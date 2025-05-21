package cn.iocoder.yudao.module.cms.dal.mysql.comment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentPageReqVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.comment.CmsCommentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface CmsCommentMapper extends BaseMapperX<CmsCommentDO> {

    default PageResult<CmsCommentDO> selectPageByArticleIdAndStatus(@Param("articleId") Long articleId,
                                                                    @Param("status") Integer status,
                                                                    PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsCommentDO>()
                .eq(CmsCommentDO::getArticleId, articleId)
                .eq(CmsCommentDO::getStatus, status)
                .orderByDesc(CmsCommentDO::getCreateTime)); // Newest comments first
    }

    default PageResult<CmsCommentDO> selectPage(CmsCommentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CmsCommentDO>()
                .eqIfPresent(CmsCommentDO::getArticleId, reqVO.getArticleId())
                .eqIfPresent(CmsCommentDO::getUserId, reqVO.getUserId())
                .eqIfPresent(CmsCommentDO::getStatus, reqVO.getStatus())
                .orderByDesc(CmsCommentDO::getCreateTime));
    }
}
