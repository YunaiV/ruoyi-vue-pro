package cn.iocoder.yudao.module.system.convert.sms;

import cn.iocoder.yudao.module.system.controller.sms.vo.channel.SysSmsChannelCreateReqVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.channel.SysSmsChannelRespVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.channel.SysSmsChannelSimpleRespVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.channel.SysSmsChannelUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsChannelDO;
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
public interface SysSmsChannelConvert {

    SysSmsChannelConvert INSTANCE = Mappers.getMapper(SysSmsChannelConvert.class);

    SysSmsChannelDO convert(SysSmsChannelCreateReqVO bean);

    SysSmsChannelDO convert(SysSmsChannelUpdateReqVO bean);

    SysSmsChannelRespVO convert(SysSmsChannelDO bean);

    List<SysSmsChannelRespVO> convertList(List<SysSmsChannelDO> list);

    PageResult<SysSmsChannelRespVO> convertPage(PageResult<SysSmsChannelDO> page);

    List<SmsChannelProperties> convertList02(List<SysSmsChannelDO> list);

    List<SysSmsChannelSimpleRespVO> convertList03(List<SysSmsChannelDO> list);

}
