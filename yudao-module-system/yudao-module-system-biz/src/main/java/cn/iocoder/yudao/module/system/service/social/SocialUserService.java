package cn.iocoder.yudao.module.system.service.social;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import me.zhyd.oauth.model.AuthUser;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 社交用户 Service 接口，例如说社交平台的授权登录
 *
 * @author 芋道源码
 */
public interface SocialUserService {

    /**
     * 获得社交平台的授权 URL
     *
     * @param type 社交平台的类型 {@link SocialTypeEnum}
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer type, String redirectUri);

    /**
     * 获得授权的用户
     * 如果授权失败，则会抛出 {@link ServiceException} 异常
     *
     * @param type 社交平台的类型 {@link SocialTypeEnum}
     * @param code 授权码
     * @param state state
     * @return 授权用户
     */
    @NotNull
    AuthUser getAuthUser(Integer type, String code, String state);

    /**
     * 获得社交用户的 unionId 编号
     *
     * @param authUser 社交用户
     * @return unionId 编号
     */
    default String getAuthUserUnionId(AuthUser authUser) {
        return StrUtil.blankToDefault(authUser.getToken().getUnionId(), authUser.getUuid());
    }

    /**
     * 获得指定用户的社交用户列表
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @return 社交用户列表
     */
    List<SocialUserDO> getSocialUserList(Long userId, Integer userType);

    /**
     * 绑定社交用户
     *
     * @param reqDTO 绑定信息
     */
    void bindSocialUser(@Valid SocialUserBindReqDTO reqDTO);

    /**
     * 取消绑定社交用户
     *
     * @param userId 用户编号
     * @param userType 全局用户类型
     * @param type 社交平台的类型 {@link SocialTypeEnum}
     * @param unionId 社交平台的 unionId
     */
    void unbindSocialUser(Long userId, Integer userType, Integer type, String unionId);

    /**
     * 校验社交用户的认证信息是否正确
     * 如果校验不通过，则抛出 {@link ServiceException} 业务异常
     *
     * @param type 社交平台的类型
     * @param code 授权码
     * @param state state
     */
    void checkSocialUser(Integer type, String code, String state);

    /**
     * 获得社交用户的绑定用户编号
     * 注意，返回的是 MemberUser 或者 AdminUser 的 id 编号！
     * 该方法会执行和 {@link #checkSocialUser(Integer, String, String)} 一样的逻辑。
     * 所以在认证信息不正确的情况下，也会抛出 {@link ServiceException} 业务异常
     *
     * @param userType 用户类型
     * @param type 社交平台的类型
     * @param code 授权码
     * @param state state
     * @return 绑定用户编号
     */
    Long getBindUserId(Integer userType, Integer type, String code, String state);
}
