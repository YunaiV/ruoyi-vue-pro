package cn.iocoder.dashboard.modules.system.service.auth.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.framework.security.config.SecurityProperties;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthGetInfoRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthGetRouterRespVO;
import cn.iocoder.dashboard.modules.system.convert.auth.SysAuthConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.dal.redis.dao.auth.SysLoginUserRedisDAO;
import cn.iocoder.dashboard.modules.system.enums.permission.MenuIdEnum;
import cn.iocoder.dashboard.modules.system.enums.user.UserStatus;
import cn.iocoder.dashboard.modules.system.service.auth.SysAuthService;
import cn.iocoder.dashboard.modules.system.service.auth.SysTokenService;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import cn.iocoder.dashboard.util.date.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * Auth Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysAuthServiceImpl implements SysAuthService {

    @Resource
    private SecurityProperties securityProperties;

    @Resource
    private SysTokenService tokenService;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private SysUserService userService;
    @Resource
    private SysRoleService roleService;
    @Resource
    private SysPermissionService permissionService;

    @Resource
    private SysLoginUserRedisDAO loginUserRedisDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 获取 username 对应的 SysUserDO
        SysUserDO user = userService.getUserByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
    }

    @Override
    public LoginUser mockLogin(Long userId) {
        // 获取用户编号对应的 SysUserDO
        SysUserDO user = userService.getUser(userId);
        if (user == null) {
            throw new UsernameNotFoundException(String.valueOf(userId));
        }
        // 创建 LoginUser 对象
        LoginUser loginUser = SysAuthConvert.INSTANCE.convert(user);
        loginUser.setUpdateTime(new Date());
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getUserId(), loginUser.getDeptId()));
        return loginUser;
    }

    @Override
    public String login(String username, String password, String captchaUUID, String captchaCode) {
        // 判断验证码是否正确
        this.verifyCaptcha(captchaUUID, captchaCode);

        // 使用账号密码，进行登陆。
        LoginUser loginUser = this.login0(username, password);
        // 缓存登陆用户到 Redis 中
        String sessionId = IdUtil.fastSimpleUUID();
        loginUser.setUpdateTime(new Date());
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getUserId(), loginUser.getDeptId()));
        loginUserRedisDAO.set(sessionId, loginUser);

        // 创建 Token
        // 我们在返回给前端的 JWT 中，使用 sessionId 作为 subject 主体，标识当前 User 用户
        return tokenService.createToken(sessionId);
    }

    private void verifyCaptcha(String captchaUUID, String captchaCode) {
        //        String verifyKey = Constants.CAPTCHA_CODE_KEY + captchaUUID;
//        String captcha = redisCache.getCacheObject(verifyKey);
//        redisCache.deleteObject(verifyKey);
//        if (captcha == null) {
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
//            throw new CaptchaExpireException();
//        }
//        if (!code.equalsIgnoreCase(captcha)) {
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
//            throw new CaptchaException();
//        }
    }

    private LoginUser login0(String username, String password) {
        // 用户验证
        Authentication authentication;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException badCredentialsException) {
            // TODO 日志优化
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        } catch (DisabledException disabledException) {
            // TODO 日志优化
            throw exception(AUTH_LOGIN_USER_DISABLED);
        } catch (AuthenticationException authenticationException) {
            // TODO 日志优化
            throw exception(AUTH_LOGIN_FAIL_UNKNOWN);
        }
        // TODO 需要优化
//        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        Assert.notNull(authentication.getPrincipal(), "Principal 不会为空");
        return (LoginUser) authentication.getPrincipal();
    }

    /**
     * 获得 User 拥有的角色编号数组
     *
     * @param userId 用户编号
     * @param deptId 科室编号
     * @return 角色编号数组
     */
    private Set<Long> getUserRoleIds(Long userId, Long deptId) {
        // 用户拥有的角色
        Set<Long> roleIds = new HashSet<>(permissionService.listUserRoleIds(userId));
        // 部门拥有的角色
        CollectionUtils.addIfNotNull(roleIds, permissionService.getDeptRoleId(deptId));
        return roleIds;
    }

    @Override
    public void logout(String token) {
//        AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, "退出成功")); TODO 需要搞一搞
    }

    @Override
    public LoginUser verifyTokenAndRefresh(String token) {
        // 验证 token 的有效性
        String sessionId = this.verifyToken(token);
        // 获得 LoginUser
        LoginUser loginUser = loginUserRedisDAO.get(sessionId);
        if (loginUser == null) {
            return null;
        }
        // 刷新 LoginUser 缓存
        this.refreshLoginUserCache(sessionId, loginUser);
        return loginUser;
    }

    private String verifyToken(String token) {
        Claims claims;
        try {
            claims = tokenService.parseToken(token);
        } catch (JwtException jwtException) {
            log.warn("[verifyToken][token({}) 解析发生异常]", token);
            return null;
        }
        // token 已经过期
        if (DateUtils.isExpired(claims.getExpiration())) {
            return null;
        }
        // 判断 sessionId 是否存在
        String sessionId = claims.getSubject();
        if (StrUtil.isBlank(sessionId)) {
            return null;
        }
        return sessionId;
    }

    private void refreshLoginUserCache(String sessionId, LoginUser loginUser) {
        // 每 1/3 的 Session 超时时间，刷新 LoginUser 缓存
        if (System.currentTimeMillis() - loginUser.getUpdateTime().getTime() <
                securityProperties.getSessionTimeout().toMillis() / 3) {
            return;
        }

        // 重新加载 SysUserDO 信息
        SysUserDO user = userService.getUser(loginUser.getUserId());
        if (user == null || UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            throw exception(TOKEN_EXPIRED); // 校验 token 时，用户被禁用的情况下，也认为 token 过期，方便前端跳转到登陆界面
        }

        // 刷新 LoginUser 缓存
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUpdateTime(new Date());
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getUserId(), loginUser.getDeptId()));
        loginUserRedisDAO.set(sessionId, loginUser);
    }

    @Override
    public SysAuthGetInfoRespVO getInfo(Long userId, Set<Long> roleIds) {
        // 获得用户信息
        SysUserDO user = userService.getUser(userId);
        if (user == null) {
            return null;
        }
        // 获得角色列表
        List<SysRoleDO> roleList = roleService.listRolesFromCache(roleIds);
        // 获得菜单列表
        List<SysMenuDO> menuList = permissionService.listRoleMenusFromCache(roleIds);
        // 拼接结果返回
        return SysAuthConvert.INSTANCE.convert(user, roleList, menuList);
    }

    @Override
    public List<SysAuthGetRouterRespVO> getRouters(Long userId, Set<Long> roleIds) {
        // TODO 芋艿：去除 F 的类型，去除 禁用 的
        List<SysMenuDO> menuList = permissionService.listRoleMenusFromCache(roleIds);
        // 转换成 Tree 结构返回
        return buildRouterTree(menuList);
    }

    private static List<SysAuthGetRouterRespVO> buildRouterTree(List<SysMenuDO> menuList) {
        // 排序，保证菜单的有序性
        menuList = new ArrayList<>(menuList); // 使用 ArrayList 套一下，因为 menuList 是不可修改的 List
        menuList.sort(Comparator.comparing(SysMenuDO::getOrderNum));
        // 构建菜单树
        // 使用 LinkedHashMap 的原因，是为了排序 。实际也可以用 Stream API ，就是太丑了。
        Map<Long, SysAuthGetRouterRespVO> treeNodeMap = new LinkedHashMap<>();
        menuList.forEach(menu -> treeNodeMap.put(menu.getMenuId(), SysAuthConvert.INSTANCE.convertTreeNode(menu)));
        // 处理父子关系
        treeNodeMap.values().stream().filter(node -> !node.getParentId().equals(MenuIdEnum.ROOT.getId())).forEach((childNode) -> {
            // 获得父节点
            SysAuthGetRouterRespVO parentNode = treeNodeMap.get(childNode.getParentId());
            if (parentNode == null) {
                log.error("[buildRouterTree][resource({}) 找不到父资源({})]", childNode.getMenuId(), childNode.getParentId());
                return;
            }
            // 将自己添加到父节点中
            if (parentNode.getChildren() == null) {
                parentNode.setChildren(new ArrayList<>());
            }
            parentNode.getChildren().add(childNode);
        });
        // 获得到所有的根节点
        return CollectionUtils.filterList(treeNodeMap.values(), node -> MenuIdEnum.ROOT.getId().equals(node.getParentId()));
    }

}
