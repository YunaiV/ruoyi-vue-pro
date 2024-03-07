package cn.iocoder.yudao.module.mp.service.handler.other;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 扫码的事件处理器
 */
@Component
public class ScanHandler implements WxMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> context,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        throw new UnsupportedOperationException("未实现该处理，请自行重写");
    }

}
