package cn.iocoder.yudao.module.system.convert.sms;

import cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel.SmsChannelCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel.SmsChannelRespVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel.SmsChannelSimpleRespVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel.SmsChannelUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 短信渠道 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SmsChannelConvert {

    SmsChannelConvert INSTANCE = Mappers.getMapper(SmsChannelConvert.class);

    SysSmsChannelDO convert(SmsChannelCreateReqVO bean);

    SysSmsChannelDO convert(SmsChannelUpdateReqVO bean);

    SmsChannelRespVO convert(SysSmsChannelDO bean);

    List<SmsChannelRespVO> convertList(List<SysSmsChannelDO> list);

    PageResult<SmsChannelRespVO> convertPage(PageResult<SysSmsChannelDO> page);

    List<SmsChannelProperties> convertList02(List<SysSmsChannelDO> list);

    List<SmsChannelSimpleRespVO> convertList03(List<SysSmsChannelDO> list);

}
