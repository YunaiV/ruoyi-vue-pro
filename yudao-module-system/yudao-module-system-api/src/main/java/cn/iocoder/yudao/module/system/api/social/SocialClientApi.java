package cn.iocoder.yudao.module.system.api.social;

import cn.iocoder.yudao.module.system.api.social.dto.SocialWxJsapiSignatureRespDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxPhoneNumberInfoRespDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;

/**
 * 社交应用的 API 接口
 *
 * @author 芋道源码
 */
public interface SocialClientApi {

    /**
     * 获得社交平台的授权 URL
     *
     * @param socialType 社交平台的类型 {@link SocialTypeEnum}
     * @param userType 用户类型
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri);

    /**
     * 创建微信公众号 JS SDK 初始化所需的签名
     *
     * @param userType 用户类型
     * @param url 访问的 URL 地址
     * @return 签名
     */
    SocialWxJsapiSignatureRespDTO createWxMpJsapiSignature(Integer userType, String url);

    /**
     * 获得微信小程序的手机信息
     *
     * @param userType 用户类型
     * @param phoneCode 手机授权码
     * @return 手机信息
     */
    SocialWxPhoneNumberInfoRespDTO getWxMaPhoneNumberInfo(Integer userType, String phoneCode);

}
