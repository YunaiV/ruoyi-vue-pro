package cn.iocoder.yudao.module.system.convert.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.SystemMailAccountBaseVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel.SmsChannelRespVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel.SmsChannelSimpleRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.SystemMailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsChannelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SystemMailAccountConvert {
    SystemMailAccountConvert INSTANCE = Mappers.getMapper(SystemMailAccountConvert.class);

    SystemMailAccountDO convert (SystemMailAccountBaseVO systemMailAccountBaseVO);

    SystemMailAccountBaseVO convert (SystemMailAccountDO systemMailAccountDO);

    PageResult<SystemMailAccountBaseVO>  convertPage(PageResult<SystemMailAccountDO> pageResult);

    List<SystemMailAccountBaseVO> convertList02(List<SystemMailAccountDO> list);
}
