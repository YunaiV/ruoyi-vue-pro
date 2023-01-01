package cn.iocoder.yudao.module.mp.convert.newsarticleitem;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newsarticleitem.WxNewsArticleItemDO;

/**
 * 图文消息文章列表表  Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxNewsArticleItemConvert {

    WxNewsArticleItemConvert INSTANCE = Mappers.getMapper(WxNewsArticleItemConvert.class);

    WxNewsArticleItemDO convert(WxNewsArticleItemCreateReqVO bean);

    WxNewsArticleItemDO convert(WxNewsArticleItemUpdateReqVO bean);

    WxNewsArticleItemRespVO convert(WxNewsArticleItemDO bean);

    List<WxNewsArticleItemRespVO> convertList(List<WxNewsArticleItemDO> list);

    PageResult<WxNewsArticleItemRespVO> convertPage(PageResult<WxNewsArticleItemDO> page);

    List<WxNewsArticleItemExcelVO> convertList02(List<WxNewsArticleItemDO> list);

}
