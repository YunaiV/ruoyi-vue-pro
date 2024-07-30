package cn.iocoder.yudao.module.system.api.social;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.social.dto.*;
import cn.iocoder.yudao.module.system.service.social.SocialClientService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.hutool.core.collection.CollUtil.findOne;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

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
        return convertList(subscribeTemplate, item -> BeanUtils.toBean(item, SocialWxSubscribeTemplateRespDTO.class)
                .setId(item.getPriTmplId()));
    }

    @Override
    public void sendSubscribeMessage(SocialWxSubscribeMessageSendReqDTO reqDTO) {
        // 1.1 获得订阅模版列表
        List<SocialWxSubscribeTemplateRespDTO> templateList = getSubscribeTemplateList(reqDTO.getUserType());
        if (CollUtil.isEmpty(templateList)) {
            log.warn("[sendSubscribeMessage][reqDTO({}) 发送订阅消息失败，原因：没有找到订阅模板]", reqDTO);
            return;
        }
        // 1.2 获得需要使用的模版
        SocialWxSubscribeTemplateRespDTO template = findOne(templateList, item ->
                ObjUtil.equal(item.getTitle(), reqDTO.getTemplateTitle()));
        if (template == null) {
            log.warn("[sendSubscribeMessage][reqDTO({}) 发送订阅消息失败，原因：没有找到订阅模板]", reqDTO);
            return;
        }

        // 2. 获得社交用户
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(reqDTO.getUserType(), reqDTO.getUserId(),
                reqDTO.getSocialType());
        if (StrUtil.isBlankIfStr(socialUser.getOpenid())) {
            log.warn("[sendSubscribeMessage][reqDTO({}) 发送订阅消息失败，原因：会员 openid 缺失]", reqDTO);
            return;
        }

        // 3. 发送订阅消息
        socialClientService.sendSubscribeMessage(reqDTO, template.getId(), socialUser.getOpenid());
    }

}
