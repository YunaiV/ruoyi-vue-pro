package cn.iocoder.yudao.module.system.convert.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountBaseVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountRespVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountSimpleRespVO;
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

    MailAccountDO convert(MailAccountBaseVO bean);

    MailAccountRespVO convert(MailAccountDO bean);

    PageResult<MailAccountBaseVO> convertPage(PageResult<MailAccountDO> pageResult);

    List<MailAccountSimpleRespVO> convertList02(List<MailAccountDO> list);

    // TODO 芋艿：改下
    default MailAccount convertAccount(MailSendMessage bean) {
        return new MailAccount()
                .setHost(bean.getHost())
                .setPort(bean.getPort())
                .setAuth(true)
                .setFrom(bean.getFromAddress())
                .setUser(bean.getUsername())
                .setPass(bean.getPassword())
                .setSslEnable(bean.getSslEnable());
    }

    // TODO 芋艿：改下
    default Map<String, String> convertToMap(MailAccountDO mailAccountDO , String content) {
        Map<String , String> map = new HashMap<>();
        map.put("from_address" , mailAccountDO.getMail());
        map.put("username" , mailAccountDO.getUsername());
        map.put("content" , content);
        return map;
    }

}
