package cn.iocoder.yudao.module.system.api.social;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.social.dto.*;
import cn.iocoder.yudao.module.system.convert.social.SocialUserConvert;
import cn.iocoder.yudao.module.system.service.social.SocialClientService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * 社交应用的 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class SocialClientApiImpl implements SocialClientApi {

    /**
     * 小程序版本
     */
    @Value("${yudao.wxa-code.env-version}")
    public String envVersion;
    @Resource
    private SocialClientService socialClientService;
    @Resource
    public SocialUserApi socialUserApi;

    @Override
    public String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri) {
        return socialClientService.getAuthorizeUrl(socialType, userType, redirectUri);
    }

    @Override
    public SocialWxJsapiSignatureRespDTO createWxMpJsapiSignature(Integer userType, String url) {
        WxJsapiSignature signature = socialClientService.createWxMpJsapiSignature(userType, url);
        return BeanUtils.toBean(signature, SocialWxJsapiSignatureRespDTO.class);
    }

    @Override
    public SocialWxPhoneNumberInfoRespDTO getWxMaPhoneNumberInfo(Integer userType, String phoneCode) {
        WxMaPhoneNumberInfo info = socialClientService.getWxMaPhoneNumberInfo(userType, phoneCode);
        return BeanUtils.toBean(info, SocialWxPhoneNumberInfoRespDTO.class);
    }

    @Override
    public byte[] getWxaQrcode(SocialWxQrcodeReqDTO reqVO) {
        return socialClientService.getWxaQrcode(reqVO);
    }

    @Override
    public List<SocialWxSubscribeTemplateRespDTO> getSubscribeTemplateList(Integer userType) {
        List<TemplateInfo> subscribeTemplate = socialClientService.getSubscribeTemplateList(userType);
        return SocialUserConvert.INSTANCE.convertList(subscribeTemplate);
    }

    @Override
    public void sendSubscribeMessage(SocialWxSubscribeMessageSendReqDTO reqDTO, Integer userType) {
        socialClientService.sendSubscribeMessage(reqDTO, userType);
    }

    public void sendSubscribeMessage(String templateTitle, Map<String, String> messages, Integer userType, Long userId,
                            Integer socialType, String path) {
        // 1.1 获得订阅模版
        SocialWxSubscribeTemplateRespDTO template = getTemplate(templateTitle, userType);
        if (template == null) {
            return;
        }
        // 1.2 获得发送对象的 openId
        String openId = getUserOpenId(userType, userId, socialType);
        if (StrUtil.isBlankIfStr(openId)) {
            return;
        }

        // 2. 发送消息
        sendSubscribeMessage(buildMessageSendReqDTO(openId, path, template).setMessages(messages), userType);
    }

    /**
     * 构建发送消息请求参数
     *
     * @param openId   接收者（用户）的 openid
     * @param path     点击模板卡片后的跳转页面，仅限本小程序内的页面
     * @param template 订阅模版
     * @return 微信小程序订阅消息发送
     */
    private SocialWxSubscribeMessageSendReqDTO buildMessageSendReqDTO(String openId, String path,
                                                                      SocialWxSubscribeTemplateRespDTO template) {
        return new SocialWxSubscribeMessageSendReqDTO().setLang("zh_CN").setMiniprogramState(envVersion)
                .setTemplateId(template.getId()).setToUser(openId).setPage(path);
    }

    /**
     * 获得小程序订阅消息模版
     *
     * @param templateTitle 模版标题
     * @param userType      用户类型
     * @return 小程序订阅消息模版
     */
    private SocialWxSubscribeTemplateRespDTO getTemplate(String templateTitle, Integer userType) {
        List<SocialWxSubscribeTemplateRespDTO> templateList = getSubscribeTemplateList(userType);
        if (CollUtil.isEmpty(templateList)) {
            log.warn("[getTemplate][templateTitle({}) userType({}) 没有找到订阅模板]", templateTitle, userType);
            return null;
        }
        return CollectionUtil.findOne(templateList, item -> ObjUtil.equal(item.getTitle(), templateTitle));
    }

    /**
     * 获得用户 openId
     *
     * @param userType   用户类型
     * @param userId     用户编号
     * @param socialType 社交类型
     * @return 用户 openId
     */
    private String getUserOpenId(Integer userType, Long userId, Integer socialType) {
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(userType, userId, socialType);
        if (StrUtil.isBlankIfStr(socialUser.getOpenid())) {
            log.warn("[getUserOpenId][userType({}) userId({}) socialType({}) 会员 openid 缺失]",
                    userType, userId, socialType);
            return null;
        }
        return socialUser.getOpenid();
    }

}
