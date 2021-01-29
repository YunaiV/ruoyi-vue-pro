package cn.iocoder.dashboard.modules.system.service.auth.impl;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.security.config.SecurityProperties;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.auth.SysUserSessionMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.auth.SysUserSessionDO;
import cn.iocoder.dashboard.modules.system.dal.redis.dao.auth.SysLoginUserRedisDAO;
import cn.iocoder.dashboard.modules.system.service.auth.SysUserSessionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 在线用户 Session Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysUserSessionServiceImpl implements SysUserSessionService {

    @Resource
    private SecurityProperties securityProperties;

    @Resource
    private SysLoginUserRedisDAO loginUserRedisDAO;

    @Resource
    private SysUserSessionMapper userSessionMapper;

    @Override
    public String createUserSession(LoginUser loginUser, String userIp, String userAgent) {
        // 生成 Session 编号
        String sessionId = generateSessionId();
        // 写入 Redis 缓存
        loginUser.setUpdateTime(new Date());
        loginUserRedisDAO.set(sessionId, loginUser);
        // 写入 DB 中
        SysUserSessionDO userSession = SysUserSessionDO.builder().userId(loginUser.getId())
                .userIp(userIp).userAgent(userAgent).build();
        userSessionMapper.insert(userSession);
        // 返回 Session 编号
        return sessionId;
    }

    @Override
    public void refreshUserSession(String sessionId, LoginUser loginUser) {
        // 写入 Redis 缓存
        loginUser.setUpdateTime(new Date());
        loginUserRedisDAO.set(sessionId, loginUser);
        // 更新 DB 中
        SysUserSessionDO updateObj = SysUserSessionDO.builder().id(sessionId).build();
        updateObj.setUpdateTime(new Date());
        userSessionMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserSession(String sessionId) {

    }

    @Override
    public LoginUser getLoginUser(String sessionId) {
        return loginUserRedisDAO.get(sessionId);
    }

    @Override
    public Long getSessionTimeoutMillis() {
        return securityProperties.getSessionTimeout().toMillis();
    }

    @Override
    public PageResult<SysUserSessionDO> getUserSessionPage(SysUserSessionPageReqVO reqVO) {
        return null;
    }

    /**
     * 生成 Session 编号，目前采用 UUID 算法
     *
     * @return Session 编号
     */
    private static String generateSessionId() {
        return IdUtil.fastSimpleUUID();
    }

}
