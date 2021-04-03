package cn.iocoder.dashboard.modules.system.convert.sms;

import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SmsChannelConvert {

    SmsChannelConvert INSTANCE = Mappers.getMapper(SmsChannelConvert.class);

    SysSmsChannelDO convert(SmsChannelCreateReqVO bean);

    SysSmsChannelDO convert(SysUserUpdateReqVO bean);

    List<SmsChannelAllVO> convert(List<SysSmsChannelDO> bean);

    List<SmsChannelProperties> convertProperty(List<SmsChannelAllVO> list);

    List<SmsChannelProperties> convertList(List<SysSmsChannelDO> list);

}
