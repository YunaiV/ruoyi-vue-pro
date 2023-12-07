package cn.iocoder.yudao.module.mp.service.handler.message;

import cn.iocoder.yudao.module.mp.framework.mp.core.context.MpContextHolder;
import cn.iocoder.yudao.module.mp.service.message.MpMessageService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 保存微信消息的事件处理器
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class MessageReceiveHandler implements WxMpMessageHandler {

    @Resource
    private MpMessageService mpMessageService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService wxMpService, WxSessionManager sessionManager) {
        log.info("[handle][接收到请求消息，内容：{}]", wxMessage);
        mpMessageService.receiveMessage(MpContextHolder.getAppId(), wxMessage);
        return null;
    }

}
