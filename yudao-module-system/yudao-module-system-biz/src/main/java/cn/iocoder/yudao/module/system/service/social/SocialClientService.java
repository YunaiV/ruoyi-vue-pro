package cn.iocoder.yudao.module.system.service.social;

import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.xingyuv.jushauth.model.AuthUser;

/**
 * 社交应用 Service 接口
 *
 * @author 芋道源码
 */
public interface SocialClientService {

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
     * 请求社交平台，获得授权的用户
     *
     * @param socialType 社交平台的类型
     * @param userType 用户类型
     * @param code 授权码
     * @param state 授权 state
     * @return 授权的用户
     */
    AuthUser getAuthUser(Integer socialType, Integer userType, String code, String state);

}
