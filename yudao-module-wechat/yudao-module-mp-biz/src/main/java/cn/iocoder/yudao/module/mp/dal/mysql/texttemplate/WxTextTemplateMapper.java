package cn.iocoder.yudao.module.mp.dal.mysql.texttemplate;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.texttemplate.WxTextTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo.*;

/**
 * 文本模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxTextTemplateMapper extends BaseMapperX<WxTextTemplateDO> {

    default PageResult<WxTextTemplateDO> selectPage(WxTextTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxTextTemplateDO>()
                .likeIfPresent(WxTextTemplateDO::getTplName, reqVO.getTplName())
                .eqIfPresent(WxTextTemplateDO::getContent, reqVO.getContent())
                .betweenIfPresent(WxTextTemplateDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxTextTemplateDO::getId));
    }

    default List<WxTextTemplateDO> selectList(WxTextTemplateExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxTextTemplateDO>()
                .likeIfPresent(WxTextTemplateDO::getTplName, reqVO.getTplName())
                .eqIfPresent(WxTextTemplateDO::getContent, reqVO.getContent())
                .betweenIfPresent(WxTextTemplateDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxTextTemplateDO::getId));
    }

}
