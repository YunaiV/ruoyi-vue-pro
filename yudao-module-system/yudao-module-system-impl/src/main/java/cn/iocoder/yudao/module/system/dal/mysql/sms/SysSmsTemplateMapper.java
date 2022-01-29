package cn.iocoder.yudao.module.system.dal.mysql.sms;

import cn.iocoder.yudao.module.system.controller.sms.vo.template.SysSmsTemplateExportReqVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.template.SysSmsTemplatePageReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysSmsTemplateMapper extends BaseMapperX<SysSmsTemplateDO> {

    default SysSmsTemplateDO selectByCode(String code) {
        return selectOne(SysSmsTemplateDO::getCode, code);
    }

    // TODO 这种参数都一样的得想办法封装一下
    default PageResult<SysSmsTemplateDO> selectPage(SysSmsTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SysSmsTemplateDO>()
                .eqIfPresent(SysSmsTemplateDO::getType, reqVO.getType())
                .eqIfPresent(SysSmsTemplateDO::getStatus, reqVO.getStatus())
                .likeIfPresent(SysSmsTemplateDO::getCode, reqVO.getCode())
                .likeIfPresent(SysSmsTemplateDO::getContent, reqVO.getContent())
                .likeIfPresent(SysSmsTemplateDO::getApiTemplateId, reqVO.getApiTemplateId())
                .eqIfPresent(SysSmsTemplateDO::getChannelId, reqVO.getChannelId())
                .betweenIfPresent(SysSmsTemplateDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(SysSmsTemplateDO::getId));
    }

    default List<SysSmsTemplateDO> selectList(SysSmsTemplateExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SysSmsTemplateDO>()
                .eqIfPresent(SysSmsTemplateDO::getType, reqVO.getType())
                .eqIfPresent(SysSmsTemplateDO::getStatus, reqVO.getStatus())
                .likeIfPresent(SysSmsTemplateDO::getCode, reqVO.getCode())
                .likeIfPresent(SysSmsTemplateDO::getContent, reqVO.getContent())
                .likeIfPresent(SysSmsTemplateDO::getApiTemplateId, reqVO.getApiTemplateId())
                .eqIfPresent(SysSmsTemplateDO::getChannelId, reqVO.getChannelId())
                .betweenIfPresent(SysSmsTemplateDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(SysSmsTemplateDO::getId));
    }

    default Integer selectCountByChannelId(Long channelId) {
        return selectCount(SysSmsTemplateDO::getChannelId, channelId);
    }

}
