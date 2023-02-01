package cn.iocoder.yudao.module.mp.service.handler.message;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpAutoReplyDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.context.MpContextHolder;
import cn.iocoder.yudao.module.mp.service.message.MpAutoReplyService;
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
 * 自动回复消息的事件处理器
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class MessageAutoReplyHandler implements WxMpMessageHandler {

    @Resource
    private MpAutoReplyService mpAutoReplyService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService weixinService, WxSessionManager sessionManager) {
        // 只处理指定类型的消息
        if (!MpAutoReplyDO.REQUEST_MESSAGE_TYPE.contains(wxMessage.getMsgType())) {
            return null;
        }

        // 自动回复
        return mpAutoReplyService.replyForMessage(MpContextHolder.getAppId(), wxMessage);
    }

}
