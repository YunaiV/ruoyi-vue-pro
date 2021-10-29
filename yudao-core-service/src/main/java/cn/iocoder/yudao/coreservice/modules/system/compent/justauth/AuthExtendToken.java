package cn.iocoder.yudao.coreservice.modules.system.compent.justauth;

import lombok.*;
import me.zhyd.oauth.model.AuthToken;

/**
 * @author timfruit
 * @date 2021-10-29
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthExtendToken extends AuthToken {
    /**
     * 微信小程序 会话密钥
     */
    private String miniSessionKey;


}
