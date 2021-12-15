package cn.iocoder.yudao.framework.social.core.model;

import lombok.*;
import me.zhyd.oauth.model.AuthToken;

/**
 * 授权所需的 token 拓展类
 *
 * @author timfruit
 * @date 2021-10-29
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthExtendToken extends AuthToken {

    /**
     * 微信小程序 - 会话密钥
     */
    private String miniSessionKey;

}
