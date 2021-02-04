package cn.iocoder.dashboard.modules.system.convert.sms;

import cn.iocoder.dashboard.common.enums.SmsChannelEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.resp.SmsChannelEnumRespVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsChannelDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SmsChannelConvert {

    SmsChannelConvert INSTANCE = Mappers.getMapper(SmsChannelConvert.class);

    @Mapping(source = "records", target = "list")
    PageResult<SmsChannelDO> convertPage(IPage<SmsChannelDO> page);

    SmsChannelDO convert(SmsChannelCreateReqVO bean);

    SmsChannelDO convert(SysUserUpdateReqVO bean);

    List<SmsChannelEnumRespVO> convertEnum(List<SmsChannelEnum> bean);

    List<SmsChannelAllVO> convert(List<SmsChannelDO> bean);

    List<SmsChannelProperty> convertProperty(List<SmsChannelAllVO> list);


}
