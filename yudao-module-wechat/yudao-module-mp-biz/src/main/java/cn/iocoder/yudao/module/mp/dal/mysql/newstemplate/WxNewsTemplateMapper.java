package cn.iocoder.yudao.module.mp.dal.mysql.newstemplate;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.newstemplate.WxNewsTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo.*;

/**
 * 图文消息模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxNewsTemplateMapper extends BaseMapperX<WxNewsTemplateDO> {

    default PageResult<WxNewsTemplateDO> selectPage(WxNewsTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxNewsTemplateDO>()
                .likeIfPresent(WxNewsTemplateDO::getTplName, reqVO.getTplName())
                .eqIfPresent(WxNewsTemplateDO::getIsUpload, reqVO.getIsUpload())
                .eqIfPresent(WxNewsTemplateDO::getMediaId, reqVO.getMediaId())
                .eqIfPresent(WxNewsTemplateDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxNewsTemplateDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxNewsTemplateDO::getId));
    }

    default List<WxNewsTemplateDO> selectList(WxNewsTemplateExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxNewsTemplateDO>()
                .likeIfPresent(WxNewsTemplateDO::getTplName, reqVO.getTplName())
                .eqIfPresent(WxNewsTemplateDO::getIsUpload, reqVO.getIsUpload())
                .eqIfPresent(WxNewsTemplateDO::getMediaId, reqVO.getMediaId())
                .eqIfPresent(WxNewsTemplateDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxNewsTemplateDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxNewsTemplateDO::getId));
    }

}
