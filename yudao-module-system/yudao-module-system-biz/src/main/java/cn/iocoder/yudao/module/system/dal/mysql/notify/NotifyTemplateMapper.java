package cn.iocoder.yudao.module.system.dal.mysql.notify;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 站内信模版 Mapper
 *
 * @author xrcoder
 */
@Mapper
public interface NotifyTemplateMapper extends BaseMapperX<NotifyTemplateDO> {

    @Select("SELECT COUNT(*) FROM system_notify_template WHERE update_time > #{maxUpdateTime}")
    Long selectCountByUpdateTimeGt(Date maxUpdateTime);

    default NotifyTemplateDO selectByCode(String code) {
        return selectOne(NotifyTemplateDO::getCode, code);
    }

    default PageResult<NotifyTemplateDO> selectPage(NotifyTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NotifyTemplateDO>()
                .eqIfPresent(NotifyTemplateDO::getCode, reqVO.getCode())
                .eqIfPresent(NotifyTemplateDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NotifyTemplateDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(NotifyTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(NotifyTemplateDO::getId));
    }

    default List<NotifyTemplateDO> selectList(NotifyTemplateExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<NotifyTemplateDO>()
                .eqIfPresent(NotifyTemplateDO::getCode, reqVO.getCode())
                .eqIfPresent(NotifyTemplateDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NotifyTemplateDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(NotifyTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(NotifyTemplateDO::getId));
    }

}
