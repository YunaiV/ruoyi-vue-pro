package cn.iocoder.yudao.framework.security.core.authentication;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 支持多用户的 UsernamePasswordAuthenticationToken 实现类
 *
 * @author 芋道源码
 */
@Getter
public class MultiUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    /**
     * 用户类型
     */
    private UserTypeEnum userType;

    public MultiUsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public MultiUsernamePasswordAuthenticationToken(Object principal, Object credentials,
                                                    Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public MultiUsernamePasswordAuthenticationToken(Object principal, Object credentials, UserTypeEnum userType) {
        super(principal, credentials);
        this.userType = userType;
    }

    public MultiUsernamePasswordAuthenticationToken(Object principal, Object credentials,
                                                    Collection<? extends GrantedAuthority> authorities, UserTypeEnum userType) {
        super(principal, credentials, authorities);
        this.userType = userType;
    }

}
