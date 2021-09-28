/*
 * MIT License
 * Copyright (c) 2020-2029 YongWu zheng (dcenter.top and gitee.com/pcore and github.com/ZeroOrInfinity)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.iocoder.yudao.adminserver.modules.system.service.auth.impl;

import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.SysUserCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.convert.auth.SysAuthConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.framework.security.core.Auth2LoginUser;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import com.google.common.collect.Sets;
import me.zhyd.oauth.model.AuthUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import top.dcenter.ums.security.core.oauth.enums.ErrorCodeEnum;
import top.dcenter.ums.security.core.oauth.exception.RegisterUserFailureException;
import top.dcenter.ums.security.core.oauth.exception.UserNotExistException;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  用户密码与手机短信登录与注册服务：<br><br>
 *  1. 用于第三方登录与手机短信登录逻辑。<br><br>
 *  2. 用于用户密码登录逻辑。<br><br>
 *  3. 用户注册逻辑。<br><br>
 * @author YongWu zheng
 * @version V1.0  Created by 2020/9/20 11:06
 */
@Service
public class UserDetailsServiceImpl implements UmsUserDetailsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false)
    private UserCache userCache;

    @Resource
    private SysUserService userService;
    
    /**
     * 用于密码加解密
     */
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SysPermissionService permissionService;

    @SuppressWarnings("AlibabaUndefineMagicConstant")
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try
        {
            // 从缓存中查询用户信息:
            // 从缓存中查询用户信息
            if (this.userCache != null)
            {
                UserDetails userDetails = this.userCache.getUserFromCache(username);
                if (userDetails != null)
                {
                    return userDetails;
                }
            }
            // 根据用户名获取用户信息
            // 获取 username 对应的 SysUserDO
            SysUserDO user = userService.getUserByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException(username);
            }
            // 创建 LoginUser 对象
            Auth2LoginUser loginUser = SysAuthConvert.INSTANCE.getAuth2LoginUser(user);
            //TODO 登录日志等可以和用户名密码等兼容处理
           return loginUser;
        }
        catch (Exception e)
        {
            String msg = String.format("第三方登录 ======>: 登录用户名：%s, 登录失败: %s", username, e.getMessage());
            log.error(msg);
            throw new UserNotExistException(ErrorCodeEnum.QUERY_USER_INFO_ERROR, e, username);
        }
    }

    @Override
    public UserDetails registerUser(@NonNull AuthUser authUser, @NonNull String username,
                                    @NonNull String defaultAuthority, String decodeState) throws RegisterUserFailureException {

        // 这里的 decodeState 可以根据自己实现的 top.dcenter.ums.security.core.oauth.service.Auth2StateCoder 接口的逻辑来传递必要的参数.
        // 比如: 第三方登录成功后的跳转地址
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 假设 decodeState 就是 redirectUrl, 我们直接把 redirectUrl 设置到 request 上
        // 后续经过成功处理器时直接从 requestAttributes.getAttribute("redirectUrl", RequestAttributes.SCOPE_REQUEST) 获取并跳转
        if (requestAttributes != null) {
            requestAttributes.setAttribute("redirectUrl", decodeState, RequestAttributes.SCOPE_REQUEST);
        }
        //返回用户
        LoginUser loginUser = doRegistUser(authUser);

        log.info("第三方用户注册 ======>: 用户名：{}, 注册成功", username);

        // 把用户信息存入缓存
        if (userCache != null)
        {
            userCache.putUserInCache(loginUser);
        }

        return loginUser;
    }

    private LoginUser doRegistUser(AuthUser authUser) {
        SysUserCreateReqVO reqVO = SysAuthConvert.INSTANCE.convert(authUser);
        if (StringUtils.isEmpty(reqVO.getPassword())) {
            reqVO.setPassword(getDefaultPassword());
        }
        //添加用户
        Long sysUserId = userService.createUser(reqVO);
        //关联第三方用户
        Long userId = userService.bindSocialUSer(sysUserId, authUser.getUuid());
        //赋予默认角色权限；三方登录默认部分
        permissionService.assignUserRole(userId, getDefaultRoles());
        LoginUser loginUser = SysAuthConvert.INSTANCE.getLoginUser(authUser);
        loginUser.setRoleIds(getDefaultRoles());
        loginUser.setPassword(getDefaultPassword());
        loginUser.setId(sysUserId);
        return loginUser;
    }

    private String getDefaultPassword() {
        return "123456";
    }

    protected Set<Long> getDefaultRoles() {
        return Sets.newHashSet(1L);
    }

    @NonNull
    public UserDetails registerUser(@NonNull String mobile, Map<String, String> otherParamMap) throws RegisterUserFailureException {

        // 用户信息持久化逻辑。。。
        // ...

        log.info("Demo ======>: 手机短信登录用户 {}：注册成功", mobile);

        User user = new User(mobile,
                             passwordEncoder.encode("admin"),
                             true,
                             true,
                             true,
                             true,
                             AuthorityUtils.commaSeparatedStringToAuthorityList("admin, ROLE_USER")
        );

        // 把用户信息存入缓存
        if (userCache != null)
        {
            userCache.putUserInCache(user);
        }

        return user;
    }

    /**
     * {@link #existedByUsernames(String...)} usernames 生成规则.
     * 如需自定义重新实现此逻辑
     * @param authUser     第三方用户信息
     * @return  返回一个 username 数组
     */
    @Override
    public String[] generateUsernames(AuthUser authUser) {
        return new String[]{
                authUser.getUsername(),
                // providerId = authUser.getSource()
                authUser.getUsername() + "_" + authUser.getSource(),
                // providerUserId = authUser.getUuid()
                authUser.getUsername() + "_" + authUser.getSource() + "_" + authUser.getUuid()
        };
    }

    @Override
    public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
        UserDetails userDetails = loadUserByUsername(userId);
        User.withUserDetails(userDetails);
        return userDetails;
    }

    @Override
    public List<Boolean> existedByUsernames(String... usernames) throws UsernameNotFoundException {
        // ... 在本地账户上查询 userIds 是否已被使用
        List<Boolean> list = new ArrayList<>();
        for (String username : usernames) {
            SysUserDO userDO = userService.getUserByUsername(username);
            list.add(userDO != null);
        }
        return list;
    }

}