package cn.iocoder.yudao.module.system.convert.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountBaseVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MailAccountConvert {
    MailAccountConvert INSTANCE = Mappers.getMapper(MailAccountConvert.class);

    MailAccountDO convert (MailAccountBaseVO mailAccountBaseVO);

    MailAccountBaseVO convert (MailAccountDO mailAccountDO);

    PageResult<MailAccountBaseVO>  convertPage(PageResult<MailAccountDO> pageResult);

    List<MailAccountBaseVO> convertList02(List<MailAccountDO> list);

    default MailAccount convertAccount(MailAccountDO mailAccountDO){
        return new MailAccount()
                .setHost(mailAccountDO.getHost())
                .setPort(mailAccountDO.getPort())
                .setAuth(true)
                .setFrom(mailAccountDO.getFrom())
                .setUser(mailAccountDO.getUsername())
                .setPass(mailAccountDO.getPassword())
                .setSslEnable(mailAccountDO.getSslEnable());
    };
}
