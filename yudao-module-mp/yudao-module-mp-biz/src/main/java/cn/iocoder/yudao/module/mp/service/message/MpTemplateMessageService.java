package cn.iocoder.yudao.module.mp.service.message;

import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;

/**
 * 公众号模板消息 Service 接口
 */
public interface MpTemplateMessageService {

    /**
     * 发送模板消息
     *
     * @param templateMessage 模板消息
     */
    void sendTemplateMessage(WxMpTemplateMessage templateMessage);

}
