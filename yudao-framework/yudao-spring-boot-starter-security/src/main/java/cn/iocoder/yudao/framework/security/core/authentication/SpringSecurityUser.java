package cn.iocoder.yudao.framework.security.core.authentication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 登录用户信息
 *
 * @author 芋道源码
 */
@Data
@AllArgsConstructor
public class SpringSecurityUser implements UserDetails {

    /**
     * 用户编号
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 租户编号
     */
    private Long tenantId;

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return CommonStatusEnum.ENABLE.getStatus().equals(status);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 返回 true，不依赖 Spring Security 判断
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 返回 true，不依赖 Spring Security 判断
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 返回 true，不依赖 Spring Security 判断
    }

}
