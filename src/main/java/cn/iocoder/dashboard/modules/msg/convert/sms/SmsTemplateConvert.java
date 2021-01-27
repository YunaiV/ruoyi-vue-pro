package cn.iocoder.dashboard.modules.msg.convert.sms;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.SmsTemplateVO;
import cn.iocoder.dashboard.modules.msg.dal.mysql.daoobject.sms.SmsChannelDO;
import cn.iocoder.dashboard.modules.msg.dal.mysql.daoobject.sms.SmsTemplateDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SmsTemplateConvert {

    SmsTemplateConvert INSTANCE = Mappers.getMapper(SmsTemplateConvert.class);

    @Mapping(source = "records", target = "list")
    PageResult<SmsChannelDO> convertPage(IPage<SmsChannelDO> page);

    List<SmsTemplateVO> convert(List<SmsTemplateDO> bean);

    SmsTemplateVO convert(SmsTemplateDO bean);

}
