package cn.iocoder.yudao.module.system.api.social;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserUnbindReqDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;

import javax.validation.Valid;

/**
 * 社交用户的 API 接口
 *
 * @author 芋道源码
 */
public interface SocialUserApi {

    /**
     * 获得社交平台的授权 URL
     *
     * @param type 社交平台的类型 {@link SocialTypeEnum}
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer type, String redirectUri);

    /**
     * 绑定社交用户
     *
     * @param reqDTO 绑定信息
     */
    void bindSocialUser(@Valid SocialUserBindReqDTO reqDTO);

    /**
     * 取消绑定社交用户
     *
     * @param reqDTO 解绑
     */
    void unbindSocialUser(@Valid SocialUserUnbindReqDTO reqDTO);

    /**
     * 获得社交用户的绑定用户编号
     * 注意，返回的是 MemberUser 或者 AdminUser 的 id 编号！
     * 在认证信息不正确的情况下，也会抛出 {@link ServiceException} 业务异常
     *
     * @param userType 用户类型
     * @param type 社交平台的类型
     * @param code 授权码
     * @param state state
     * @return 绑定用户编号
     */
    Long getBindUserId(Integer userType, Integer type, String code, String state);

}
