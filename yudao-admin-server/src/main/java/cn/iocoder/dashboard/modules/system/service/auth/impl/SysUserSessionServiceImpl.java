package cn.iocoder.dashboard.modules.system.service.auth.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.util.monitor.TracerUtils;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.dashboard.modules.system.dal.redis.auth.SysLoginUserRedisDAO;
import cn.iocoder.dashboard.modules.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.dashboard.modules.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.dashboard.modules.system.service.auth.SysUserSessionService;
import cn.iocoder.dashboard.modules.system.service.logger.SysLoginLogService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.util.date.DateUtils.addTime;

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
    @Resource
    private SysUserService userService;
    @Resource
    private SysLoginLogService loginLogService;

    @Override
    public String createUserSession(LoginUser loginUser, String userIp, String userAgent) {
        // 生成 Session 编号
        String sessionId = generateSessionId();
        // 写入 Redis 缓存
        loginUser.setUpdateTime(new Date());
        loginUserRedisDAO.set(sessionId, loginUser);
        // 写入 DB 中
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userIp(userIp).userAgent(userAgent).username(loginUser.getUsername())
                .sessionTimeout(addTime(Duration.ofMillis(getSessionTimeoutMillis())))
                .build();
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
        updateObj.setUsername(loginUser.getUsername());
        updateObj.setUpdateTime(new Date());
        updateObj.setSessionTimeout(addTime(Duration.ofMillis(getSessionTimeoutMillis())));
        userSessionMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserSession(String sessionId) {
        // 删除 Redis 缓存
        loginUserRedisDAO.delete(sessionId);
        // 删除 DB 记录
        userSessionMapper.deleteById(sessionId);
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
        // 处理基于用户昵称的查询
        Collection<Long> userIds = null;
        if (StrUtil.isNotEmpty(reqVO.getUsername())) {
            userIds = convertSet(userService.getUsersByUsername(reqVO.getUsername()), SysUserDO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return PageResult.empty();
            }
        }
        return userSessionMapper.selectPage(reqVO, userIds);
    }

    @Override
    public long clearSessionTimeout() {
        // 获取db里已经超时的用户列表
        List<SysUserSessionDO> sessionTimeoutDOS = userSessionMapper.selectListBySessionTimoutLt();
        Map<String, SysUserSessionDO> timeoutSessionDOMap = sessionTimeoutDOS
                .stream()
                .filter(sessionDO -> loginUserRedisDAO.get(sessionDO.getId()) == null)
                .collect(Collectors.toMap(SysUserSessionDO::getId, o -> o));
        // 确认已经超时,按批次移出在线用户列表
        if (CollUtil.isNotEmpty(timeoutSessionDOMap)) {
            Lists.partition(new ArrayList<>(timeoutSessionDOMap.keySet()), 100).forEach(userSessionMapper::deleteBatchIds);
            //记录用户超时退出日志
            createTimeoutLogoutLog(timeoutSessionDOMap.values());
        }
        return timeoutSessionDOMap.size();
    }

    private void createTimeoutLogoutLog(Collection<SysUserSessionDO> timeoutSessionDOS) {
        for (SysUserSessionDO timeoutSessionDO : timeoutSessionDOS) {
            SysLoginLogCreateReqVO reqVO = new SysLoginLogCreateReqVO();
            reqVO.setLogType(SysLoginLogTypeEnum.LOGOUT_TIMEOUT.getType());
            reqVO.setTraceId(TracerUtils.getTraceId());
            reqVO.setUsername(timeoutSessionDO.getUsername());
            reqVO.setUserAgent(timeoutSessionDO.getUserAgent());
            reqVO.setUserIp(timeoutSessionDO.getUserIp());
            reqVO.setResult(SysLoginResultEnum.SUCCESS.getResult());
            loginLogService.createLoginLog(reqVO);
        }
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
