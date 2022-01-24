package cn.iocoder.yudao.framework.security.core;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

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
     * 用户类型
     *
     * 关联 {@link UserTypeEnum}
     */
    private Integer userType;
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
    /**
     * 租户编号
     */
    private Long tenantId;

    // ========== UserTypeEnum.ADMIN 独有字段 ==========
    // TODO 芋艿：可以通过定义一个 Map<String, String> exts 的方式，去除管理员的字段。不过这样会导致系统比较复杂，所以暂时不去掉先；
    /**
     * 角色编号数组
     */
    private Set<Long> roleIds;
    /**
     * 部门编号
     */
    private Long deptId;
    /**
     * 所属岗位
     */
    private Set<Long> postIds;

    // ========== 上下文 ==========
    /**
     * 上下文字段，不进行持久化
     *
     * 1. 用于基于 LoginUser 维度的临时缓存
     */
    @JsonIgnore
    private Map<String, Object> context;

    @Override
    @JsonIgnore// 避免序列化
    public String getPassword() {
        return password;
    }

    @Override
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

    // ========== 上下文 ==========

    public void setContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
    }

    public <T> T getContext(String key, Class<T> type) {
        return MapUtil.get(context, key, type);
    }

}
