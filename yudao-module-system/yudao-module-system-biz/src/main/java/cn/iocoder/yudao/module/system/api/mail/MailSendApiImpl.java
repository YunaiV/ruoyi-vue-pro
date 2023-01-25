package cn.iocoder.yudao.module.system.api.mail;

import cn.iocoder.yudao.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 邮件发送 API 接口
 *
 * @author wangjingyi
 */
@Service
@Validated
public class MailSendApiImpl implements MailSendApi {

    @Override
    public Long sendSingleSmsToAdmin(MailSendSingleToUserReqDTO reqDTO) {
        return null;
    }

    @Override
    public Long sendSingleSmsToMember(MailSendSingleToUserReqDTO reqDTO) {
        return null;
    }

}
