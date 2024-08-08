package cn.iocoder.yudao.module.mp.service.handler.message;

import cn.iocoder.yudao.module.mp.framework.mp.core.context.MpContextHolder;
import cn.iocoder.yudao.module.mp.service.message.MpTemplateMessageService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 模板消息的事件处理器
 */
@Component
@Slf4j
public class TemplateMessageHandler implements WxMpMessageHandler {

    @Resource
    private MpTemplateMessageService mpTemplateMessageService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService wxMpService, WxSessionManager sessionManager) {
        log.info("[handle][接收到模板消息，内容：{}]", wxMessage);
        // 处理模板消息的逻辑
        mpTemplateMessageService.sendTemplateMessage(MpContextHolder.getAppId(), wxMessage);
        return null;
    }

}
