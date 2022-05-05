package cn.iocoder.yudao.module.system.convert.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountBaseVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface MailAccountConvert {
    MailAccountConvert INSTANCE = Mappers.getMapper(MailAccountConvert.class);

    MailAccountDO convert (MailAccountBaseVO mailAccountBaseVO);

    MailAccountBaseVO convert (MailAccountDO mailAccountDO);

    PageResult<MailAccountBaseVO>  convertPage(PageResult<MailAccountDO> pageResult);

    List<MailAccountBaseVO> convertList02(List<MailAccountDO> list);

    default MailAccount convertAccount(MailSendMessage mailAccountDO){
        return new MailAccount()
                .setHost(mailAccountDO.getHost())
                .setPort(mailAccountDO.getPort())
                .setAuth(true)
                .setFrom(mailAccountDO.getFromAddress())
                .setUser(mailAccountDO.getUsername())
                .setPass(mailAccountDO.getPassword())
                .setSslEnable(mailAccountDO.getSslEnable());
    }

    default Map<String, String> convertToMap(MailAccountDO mailAccountDO , String content) {
        Map<String , String> map = new HashMap<>();
        map.put("from_address" , mailAccountDO.getFromAddress());
        map.put("username" , mailAccountDO.getUsername());
        map.put("content" , content);
        return map;
    }

}
