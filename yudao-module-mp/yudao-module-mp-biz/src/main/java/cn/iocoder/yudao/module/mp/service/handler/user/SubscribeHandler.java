package cn.iocoder.yudao.module.mp.service.handler.user;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.mp.builder.TextBuilder;
import cn.iocoder.yudao.module.mp.controller.admin.user.vo.WxAccountFansCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.user.vo.WxAccountFansUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.subscribetext.WxSubscribeTextDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.texttemplate.WxTextTemplateDO;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import cn.iocoder.yudao.module.mp.service.user.MpUserService;
import cn.iocoder.yudao.module.mp.service.accountfanstag.WxAccountFansTagService;
import cn.iocoder.yudao.module.mp.service.subscribetext.WxSubscribeTextService;
import cn.iocoder.yudao.module.mp.service.texttemplate.WxTextTemplateService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
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

    @Autowired
    private WxTextTemplateService wxTextTemplateService;

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpAccountService mpAccountService;

    @Autowired
    private WxSubscribeTextService wxSubscribeTextService;

    @Autowired
    private MpUserService wxAccountFansService;

    @Autowired
    private WxAccountFansTagService wxAccountFansTagService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) throws WxErrorException {

        log.info("新关注用户 OPENID: " + wxMessage.getFromUser());

        // 获取微信用户基本信息
        try {
            WxMpUser wxmpUser = weixinService.getUserService()
                    .userInfo(wxMessage.getFromUser(), null);
            if (wxmpUser != null) {
                // 可以添加关注用户到本地数据库
                MpAccountDO wxAccount = mpAccountService.findBy(MpAccountDO::getAccount, wxMessage.getToUser());
                if (wxAccount != null) {
                    MpUserDO wxAccountFans = wxAccountFansService.findBy(MpUserDO::getOpenid, wxmpUser.getOpenId());
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
                        wxAccountFansCreateReqVO.setLanguage(wxmpUser.getLanguage());
                        wxAccountFansCreateReqVO.setHeadimgUrl(wxmpUser.getHeadImgUrl());
                        wxAccountFansCreateReqVO.setRemark(wxmpUser.getRemark());
                        wxAccountFansCreateReqVO.setWxAccountId(String.valueOf(wxAccount.getId()));
                        wxAccountFansCreateReqVO.setWxAccountAppid(wxAccount.getAppId());
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
                        wxAccountFansUpdateReqVO.setLanguage(wxmpUser.getLanguage());
                        wxAccountFansUpdateReqVO.setHeadimgUrl(wxmpUser.getHeadImgUrl());
                        wxAccountFansUpdateReqVO.setRemark(wxmpUser.getRemark());
                        wxAccountFansUpdateReqVO.setWxAccountId(String.valueOf(wxAccount.getId()));
                        wxAccountFansUpdateReqVO.setWxAccountAppid(wxAccount.getAppId());
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
            MpAccountDO wxAccount = mpAccountService.findBy(MpAccountDO::getAccount, wxMessage.getToUser());
            if (wxAccount != null) {
                WxSubscribeTextDO wxSubscribeText = wxSubscribeTextService.findBy(WxSubscribeTextDO::getWxAccountId, String.valueOf(wxAccount.getId()));
                if (wxSubscribeText != null) {
                    WxTextTemplateDO wxTextTemplate = wxTextTemplateService.getWxTextTemplate(Integer.parseInt(wxSubscribeText.getTplId()));
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
