package cn.iocoder.yudao.module.mp.service.handler.user;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.mp.framework.mp.core.context.MpContextHolder;
import cn.iocoder.yudao.module.mp.service.message.MpAutoReplyService;
import cn.iocoder.yudao.module.mp.service.user.MpUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.error.WxMpErrorMsgEnum;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 关注的事件处理器
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SubscribeHandler implements WxMpMessageHandler {

    @Resource
    private MpUserService mpUserService;
    @Resource
    private MpAutoReplyService mpAutoReplyService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService weixinService, WxSessionManager sessionManager) throws WxErrorException {
        // 第一步，从公众号平台，获取粉丝信息
        log.info("[handle][粉丝({}) 关注]", wxMessage.getFromUser());
        WxMpUser wxMpUser = null;
        try {
            wxMpUser = weixinService.getUserService().userInfo(wxMessage.getFromUser());
        } catch (WxErrorException e) {
            log.error("[handle][粉丝({})] 获取粉丝信息失败！", wxMessage.getFromUser(), e);
            // 特殊情况（个人账号，无接口权限）：https://t.zsxq.com/cLFq5
            if (ObjUtil.equal(e.getError().getErrorCode(), WxMpErrorMsgEnum.CODE_48001)) {
                wxMpUser = new WxMpUser();
                wxMpUser.setOpenId(wxMessage.getFromUser());
                wxMpUser.setSubscribe(true);
                wxMpUser.setSubscribeTime(System.currentTimeMillis() / 1000L);
            }
        }

        // 第二步，保存粉丝信息
        mpUserService.saveUser(MpContextHolder.getAppId(), wxMpUser);

        // 第三步，回复关注的欢迎语
        return mpAutoReplyService.replyForSubscribe(MpContextHolder.getAppId(), wxMessage);
    }

}
