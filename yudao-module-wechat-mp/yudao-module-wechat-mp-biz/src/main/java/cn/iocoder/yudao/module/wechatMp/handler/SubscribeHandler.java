package cn.iocoder.yudao.module.wechatMp.handler;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.wechatMp.builder.TextBuilder;
import cn.iocoder.yudao.module.wechatMp.controller.admin.accountfans.vo.WxAccountFansCreateReqVO;
import cn.iocoder.yudao.module.wechatMp.controller.admin.accountfans.vo.WxAccountFansUpdateReqVO;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.accountfans.WxAccountFansDO;
import cn.iocoder.yudao.module.wechatMp.service.account.WxAccountService;
import cn.iocoder.yudao.module.wechatMp.service.accountfans.WxAccountFansService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Component
@Slf4j
public class SubscribeHandler implements WxMpMessageHandler {

   /* @Autowired
    private WxTextTemplateService wxTextTemplateService;

    @Autowired
    private WxAccountService wxAccountService;

    @Autowired
    private WxSubscribeTextService wxSubscribeTextService;

    @Autowired
    private WxAccountFansService wxAccountFansService;

    @Autowired
    private WxAccountFansTagService wxAccountFansTagService;*/

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) throws WxErrorException {

        log.info("新关注用户 OPENID: " + wxMessage.getFromUser());
/*

        // 获取微信用户基本信息
        try {
            WxMpUser wxmpUser = weixinService.getUserService()
                    .userInfo(wxMessage.getFromUser(), null);
            if (wxmpUser != null) {
                // 可以添加关注用户到本地数据库
                WxAccountDO wxAccount = wxAccountService.findBy(WxAccountDO::getAccount, wxMessage.getToUser());
                if (wxAccount != null) {
                    WxAccountFansDO wxAccountFans = wxAccountFansService.findBy(WxAccountFansDO::getOpenid, wxmpUser.getOpenId());
                    if (wxAccountFans == null) {//insert
                        WxAccountFansCreateReqVO wxAccountFansCreateReqVO = new WxAccountFansCreateReqVO();
                        wxAccountFansCreateReqVO.setOpenid(wxmpUser.getOpenId());
                        wxAccountFansCreateReqVO.setSubscribeStatus(wxmpUser.getSubscribe() ? "1" : "0");
                        wxAccountFansCreateReqVO.setSubscribeTime(DateUtil.date(wxmpUser.getSubscribeTime() * 1000L));
                        try {
                            wxAccountFansCreateReqVO.setNickname(wxmpUser.getNickname().getBytes("UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        wxAccountFansCreateReqVO.setGender(String.valueOf(wxmpUser.getSex()));
                        wxAccountFansCreateReqVO.setLanguage(wxmpUser.getLanguage());
                        wxAccountFansCreateReqVO.setCountry(wxmpUser.getCountry());
                        wxAccountFansCreateReqVO.setProvince(wxmpUser.getProvince());
                        wxAccountFansCreateReqVO.setCity(wxmpUser.getCity());
                        wxAccountFansCreateReqVO.setHeadimgUrl(wxmpUser.getHeadImgUrl());
                        wxAccountFansCreateReqVO.setRemark(wxmpUser.getRemark());
                        wxAccountFansCreateReqVO.setWxAccountId(String.valueOf(wxAccount.getId()));
                        wxAccountFansCreateReqVO.setWxAccountAppid(wxAccount.getAppid());
                        wxAccountFansService.createWxAccountFans(wxAccountFansCreateReqVO);

                        //process tags
                        wxAccountFansTagService.processFansTags(wxAccount, wxmpUser);

                    } else {//update
                        WxAccountFansUpdateReqVO wxAccountFansUpdateReqVO = new WxAccountFansUpdateReqVO();
                        wxAccountFansUpdateReqVO.setId(wxAccountFans.getId());
                        wxAccountFansUpdateReqVO.setOpenid(wxmpUser.getOpenId());
                        wxAccountFansUpdateReqVO.setSubscribeStatus(wxmpUser.getSubscribe() ? "1" : "0");
                        wxAccountFansUpdateReqVO.setSubscribeTime(DateUtil.date(wxmpUser.getSubscribeTime() * 1000L));
                        try {
                            wxAccountFans.setNickname(wxmpUser.getNickname().getBytes("UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        wxAccountFansUpdateReqVO.setGender(String.valueOf(wxmpUser.getSex()));
                        wxAccountFansUpdateReqVO.setLanguage(wxmpUser.getLanguage());
                        wxAccountFansUpdateReqVO.setCountry(wxmpUser.getCountry());
                        wxAccountFansUpdateReqVO.setProvince(wxmpUser.getProvince());
                        wxAccountFansUpdateReqVO.setCity(wxmpUser.getCity());
                        wxAccountFansUpdateReqVO.setHeadimgUrl(wxmpUser.getHeadImgUrl());
                        wxAccountFansUpdateReqVO.setRemark(wxmpUser.getRemark());
                        wxAccountFansUpdateReqVO.setWxAccountId(String.valueOf(wxAccount.getId()));
                        wxAccountFansUpdateReqVO.setWxAccountAppid(wxAccount.getAppid());
                        wxAccountFansService.updateWxAccountFans(wxAccountFansUpdateReqVO);

                        //process tags
                        wxAccountFansTagService.processFansTags(wxAccount, wxmpUser);

                    }
                }

            }
        } catch (WxErrorException e) {
            if (e.getError().getErrorCode() == 48001) {
                log.info("该公众号没有获取用户信息权限！");
            }
        }


        WxMpXmlOutMessage responseResult = null;
        try {
            responseResult = this.handleSpecial(wxMessage);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (responseResult != null) {
            return responseResult;
        }

        try {
            String content = "感谢关注！";//默认
            WxAccountDO wxAccount = wxAccountService.findBy(WxAccountDO::getAccount, wxMessage.getToUser());
            if (wxAccount != null) {
                WxSubscribeText wxSubscribeText = wxSubscribeTextService.findBy("wxAccountId", String.valueOf(wxAccount.getId()));
                if (wxSubscribeText != null) {
                    WxTextTemplate wxTextTemplate = wxTextTemplateService.findById(Integer.parseInt(wxSubscribeText.getTplId()));
                    if (wxTextTemplate != null) {
                        content = wxTextTemplate.getContent();
                    }
                }
            }
            log.info("wxMessage : {}", JsonUtils.toJsonString(wxMessage));
            return new TextBuilder().build(content, wxMessage, weixinService);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
*/

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
