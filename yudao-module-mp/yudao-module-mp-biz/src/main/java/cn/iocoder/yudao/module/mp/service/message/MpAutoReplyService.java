package cn.iocoder.yudao.module.mp.service.message;

import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * 公众号的自动回复 Service 接口
 *
 * @author 芋道源码
 */
public interface MpAutoReplyService {

    /**
     * 当收到消息时，自动回复
     *
     * @param appId 微信公众号 appId
     * @param wxMessage 消息
     * @return 回复的消息
     */
    WxMpXmlOutMessage replyForMessage(String appId, WxMpXmlMessage wxMessage);

}
