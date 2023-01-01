package cn.iocoder.yudao.module.mp.dal.mysql.newsarticleitem;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.newsarticleitem.WxNewsArticleItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo.*;

/**
 * 图文消息文章列表表  Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxNewsArticleItemMapper extends BaseMapperX<WxNewsArticleItemDO> {

    default PageResult<WxNewsArticleItemDO> selectPage(WxNewsArticleItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxNewsArticleItemDO>()
                .eqIfPresent(WxNewsArticleItemDO::getTitle, reqVO.getTitle())
                .eqIfPresent(WxNewsArticleItemDO::getDigest, reqVO.getDigest())
                .eqIfPresent(WxNewsArticleItemDO::getAuthor, reqVO.getAuthor())
                .eqIfPresent(WxNewsArticleItemDO::getShowCoverPic, reqVO.getShowCoverPic())
                .eqIfPresent(WxNewsArticleItemDO::getThumbMediaId, reqVO.getThumbMediaId())
                .eqIfPresent(WxNewsArticleItemDO::getContent, reqVO.getContent())
                .eqIfPresent(WxNewsArticleItemDO::getContentSourceUrl, reqVO.getContentSourceUrl())
                .eqIfPresent(WxNewsArticleItemDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(WxNewsArticleItemDO::getPicPath, reqVO.getPicPath())
                .eqIfPresent(WxNewsArticleItemDO::getNeedOpenComment, reqVO.getNeedOpenComment())
                .eqIfPresent(WxNewsArticleItemDO::getOnlyFansCanComment, reqVO.getOnlyFansCanComment())
                .eqIfPresent(WxNewsArticleItemDO::getNewsId, reqVO.getNewsId())
                .eqIfPresent(WxNewsArticleItemDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxNewsArticleItemDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxNewsArticleItemDO::getId));
    }

    default List<WxNewsArticleItemDO> selectList(WxNewsArticleItemExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxNewsArticleItemDO>()
                .eqIfPresent(WxNewsArticleItemDO::getTitle, reqVO.getTitle())
                .eqIfPresent(WxNewsArticleItemDO::getDigest, reqVO.getDigest())
                .eqIfPresent(WxNewsArticleItemDO::getAuthor, reqVO.getAuthor())
                .eqIfPresent(WxNewsArticleItemDO::getShowCoverPic, reqVO.getShowCoverPic())
                .eqIfPresent(WxNewsArticleItemDO::getThumbMediaId, reqVO.getThumbMediaId())
                .eqIfPresent(WxNewsArticleItemDO::getContent, reqVO.getContent())
                .eqIfPresent(WxNewsArticleItemDO::getContentSourceUrl, reqVO.getContentSourceUrl())
                .eqIfPresent(WxNewsArticleItemDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(WxNewsArticleItemDO::getPicPath, reqVO.getPicPath())
                .eqIfPresent(WxNewsArticleItemDO::getNeedOpenComment, reqVO.getNeedOpenComment())
                .eqIfPresent(WxNewsArticleItemDO::getOnlyFansCanComment, reqVO.getOnlyFansCanComment())
                .eqIfPresent(WxNewsArticleItemDO::getNewsId, reqVO.getNewsId())
                .eqIfPresent(WxNewsArticleItemDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxNewsArticleItemDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxNewsArticleItemDO::getId));
    }

}
