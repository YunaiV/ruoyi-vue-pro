package cn.iocoder.yudao.module.mp.service.message;

import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

@Service
@Validated
@Slf4j
public class MpTemplateMessageServiceImpl implements MpTemplateMessageService {

    @Resource
    private MpServiceFactory mpServiceFactory;

    @Override
    public void sendTemplateMessage(WxMpTemplateMessage templateMessage) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(templateMessage.getAppid());
        try {
            mpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            log.error("[sendTemplateMessage][发送模板消息失败，templateMessage={}]", templateMessage, e);
            throw new RuntimeException("发送模板消息失败", e);
        }
    }

}
