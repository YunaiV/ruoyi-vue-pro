package cn.iocoder.yudao.adminserver.modules.activiti.service.config;


import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysPostService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

@Service
public class UserGroupManagerService implements UserGroupManager {

    @Resource
    private  UserDetailsService userDetailsService;

    @Resource
    private SysUserService userService;

    @Resource
    private SysPostService  sysPostService;

    /**
     * 暂时使用岗位来代替
     * @param userId
     * @return
     */
    @Override
    public List<String> getUserGroups(String userId) {
//        final LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(userId);
//        final Long id = loginUser.getId();
        final SysUserDO user = userService.getUserByUsername(userId);
        return  sysPostService.getPosts(user.getPostIds()).stream().map(post -> post.getCode()).collect(Collectors.toList());

    }

    @Override
    public List<String> getUserRoles(String userId) {
       return Arrays.asList("ROLE_ACTIVITI_USER");
    }

    @Override
    public List<String> getGroups() {
        throw new UnsupportedOperationException("getGroups is now un supported");
    }

    @Override
    public List<String> getUsers() {
        throw new UnsupportedOperationException("getGroups is now un supported");
    }
}
