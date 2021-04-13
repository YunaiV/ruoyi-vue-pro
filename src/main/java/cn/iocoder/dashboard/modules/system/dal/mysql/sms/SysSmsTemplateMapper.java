package cn.iocoder.dashboard.modules.system.dal.mysql.sms;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplatePageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface SysSmsTemplateMapper extends BaseMapperX<SysSmsTemplateDO> {

    default SysSmsTemplateDO selectByCode(String code) {
        return selectOne("code", code);
    }

    default PageResult<SysSmsTemplateDO> selectPage(SysSmsTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysSmsTemplateDO>()
                .eqIfPresent("type", reqVO.getType())
                .eqIfPresent("status", reqVO.getStatus())
                .likeIfPresent("code", reqVO.getCode())
                .likeIfPresent("content", reqVO.getContent())
                .likeIfPresent("api_template_id", reqVO.getApiTemplateId())
                .eqIfPresent("channel_id", reqVO.getChannelId())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default List<SysSmsTemplateDO> selectList(SysSmsTemplateExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysSmsTemplateDO>()
                .eqIfPresent("type", reqVO.getType())
                .eqIfPresent("status", reqVO.getStatus())
                .likeIfPresent("code", reqVO.getCode())
                .likeIfPresent("content", reqVO.getContent())
                .likeIfPresent("api_template_id", reqVO.getApiTemplateId())
                .eqIfPresent("channel_id", reqVO.getChannelId())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default Integer selectCountByChannelId(Long channelId) {
        return selectCount("channel_id", channelId);
    }

    @Select("SELECT id FROM sys_sms_template WHERE update_time > #{maxUpdateTime} LIMIT 1")
    Long selectExistsByUpdateTimeAfter(Date maxUpdateTime);

}
