package cn.iocoder.dashboard.modules.system.convert.sms;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsTemplateVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SmsTemplateConvert {

    SmsTemplateConvert INSTANCE = Mappers.getMapper(SmsTemplateConvert.class);

    @Mapping(source = "records", target = "list")
    PageResult<SysSmsChannelDO> convertPage(IPage<SysSmsChannelDO> page);

    List<SmsTemplateVO> convert(List<SysSmsTemplateDO> bean);

    SmsTemplateVO convert(SysSmsTemplateDO bean);

}
