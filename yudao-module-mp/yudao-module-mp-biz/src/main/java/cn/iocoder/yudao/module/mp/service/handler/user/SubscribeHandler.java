package cn.iocoder.yudao.module.mp.service.handler.user;

import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.context.MpContextHolder;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import cn.iocoder.yudao.module.mp.service.user.MpUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 关注的事件处理器
 *
 * @author 芋道源码
 *
 * // TODO 芋艿：待实现
 */
@Component
@Slf4j
public class SubscribeHandler implements WxMpMessageHandler {

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpAccountService mpAccountService;

    @Resource
    private MpUserService mpUserService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService weixinService, WxSessionManager sessionManager) throws WxErrorException {
        // 第一步，从公众号平台，获取用户信息
        log.info("[handle][用户({}) 关注]", wxMessage.getFromUser());
        WxMpUser wxMpUser = null;
        try {
            wxMpUser = weixinService.getUserService().userInfo(wxMessage.getFromUser());
        } catch (WxErrorException e) {
            log.error("[handle][用户({})] 获取用户信息失败！", wxMessage.getFromUser(), e);
        }

        // 第二步，保存用户信息
        MpUserDO mpUser = null;
        if (wxMpUser != null) {
            mpUser = mpUserService.saveUser(MpContextHolder.getAppId(), wxMpUser);
        }

        // 第三步，回复关注的欢迎语  TODO 芋艿：关注的欢迎语
//        return new TextBuilder().build("感谢关注", wxMessage, weixinService);
        return null;
    }

    /**
     * 处理特殊请求，比如如果是扫码进来的，可以做相应处理
     */
    private WxMpXmlOutMessage handleSpecial(WxMpXmlMessage wxMessage)
            throws Exception {

        //TODO
        return null;
    }

}
