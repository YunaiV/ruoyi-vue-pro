package cn.iocoder.yudao.module.system.api.social;

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
     * @param type 社交平台的类型 {@link SocialTypeEnum}
     * @param userType 用户类型
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer type, Integer userType, String redirectUri);

}
