package cn.iocoder.yudao.module.mp.handler;

import cn.iocoder.yudao.module.mp.controller.admin.accountfans.vo.WxAccountFansUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfans.WxAccountFansDO;
import cn.iocoder.yudao.module.mp.service.account.WxAccountService;
import cn.iocoder.yudao.module.mp.service.accountfans.WxAccountFansService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class UnsubscribeHandler implements WxMpMessageHandler {

    @Autowired
    private WxAccountService wxAccountService;

    @Autowired
    private WxAccountFansService wxAccountFansService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) {
        String openId = wxMessage.getFromUser();
        log.info("取消关注用户 OPENID: " + openId);
        // TODO 可以更新本地数据库为取消关注状态

        MpAccountDO wxAccountDO = wxAccountService.findBy(MpAccountDO::getAccount, wxMessage.getToUser());
        if (wxAccountDO != null) {
            WxAccountFansDO wxAccountFansDO = wxAccountFansService.findBy(WxAccountFansDO::getOpenid, openId);
            if (wxAccountFansDO != null) {
                WxAccountFansUpdateReqVO wxAccountFansUpdateReqVO = new WxAccountFansUpdateReqVO();
                wxAccountFansUpdateReqVO.setId(wxAccountDO.getId());
                wxAccountFansDO.setSubscribeStatus("0");//取消订阅
                wxAccountFansService.updateWxAccountFans(wxAccountFansUpdateReqVO);
            }
        }


        return null;
    }

}
