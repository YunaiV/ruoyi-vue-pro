package cn.iocoder.yudao.module.system.convert.mail;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.*;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MailAccountConvert {

    MailAccountConvert INSTANCE = Mappers.getMapper(MailAccountConvert.class);

    MailAccountDO convert(MailAccountCreateReqVO bean);

    MailAccountDO convert(MailAccountUpdateReqVO bean);

    MailAccountRespVO convert(MailAccountDO bean);

    PageResult<MailAccountBaseVO> convertPage(PageResult<MailAccountDO> pageResult);

    List<MailAccountSimpleRespVO> convertList02(List<MailAccountDO> list);

    default MailAccount convert(MailAccountDO account, String nickname) {
        String from = StrUtil.isNotEmpty(nickname) ? nickname + " <" + account.getMail() + ">" : account.getMail();
        return new MailAccount().setFrom(from).setAuth(true)
                .setUser(account.getUsername()).setPass(account.getPassword())
                .setHost(account.getHost()).setPort(account.getPort()).setSslEnable(account.getSslEnable());
    }

}
