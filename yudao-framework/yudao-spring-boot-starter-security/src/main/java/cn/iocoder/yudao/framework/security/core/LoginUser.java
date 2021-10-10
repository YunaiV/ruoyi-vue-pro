package cn.iocoder.yudao.framework.security.core;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录用户信息
 *
 * @author 芋道源码
 */
@Data
public class LoginUser implements UserDetails {

    /**
     * 用户编号
     */
    private Long id;
    /**
     * 科室编号
     */
    private Long deptId;
    /**
     * 角色编号数组
     */
    private Set<Long> roleIds;
    /**
     * 最后更新时间
     */
    private Date updateTime;

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

    @Override
    @JsonIgnore// 避免序列化
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore// 避免序列化
    public boolean isEnabled() {
        return CommonStatusEnum.ENABLE.getStatus().equals(status);
    }

    @Override
    @JsonIgnore// 避免序列化
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>();
    }

    @Override
    @JsonIgnore// 避免序列化
    public boolean isAccountNonExpired() {
        return true; // 返回 true，不依赖 Spring Security 判断
    }

    @Override
    @JsonIgnore// 避免序列化
    public boolean isAccountNonLocked() {
        return true; // 返回 true，不依赖 Spring Security 判断
    }

    @Override
    @JsonIgnore// 避免序列化
    public boolean isCredentialsNonExpired() {
        return true;  // 返回 true，不依赖 Spring Security 判断
    }

}
