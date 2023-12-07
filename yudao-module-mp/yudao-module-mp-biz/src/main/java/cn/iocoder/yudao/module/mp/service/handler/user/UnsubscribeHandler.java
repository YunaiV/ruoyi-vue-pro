package cn.iocoder.yudao.module.mp.service.handler.user;

import cn.iocoder.yudao.module.mp.framework.mp.core.context.MpContextHolder;
import cn.iocoder.yudao.module.mp.service.user.MpUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 取消关注的事件处理器
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class UnsubscribeHandler implements WxMpMessageHandler {

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpUserService mpUserService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) {
        log.info("[handle][粉丝({}) 取消关注]", wxMessage.getFromUser());
        mpUserService.updateUserUnsubscribe(MpContextHolder.getAppId(), wxMessage.getFromUser());
        return null;
    }

}
