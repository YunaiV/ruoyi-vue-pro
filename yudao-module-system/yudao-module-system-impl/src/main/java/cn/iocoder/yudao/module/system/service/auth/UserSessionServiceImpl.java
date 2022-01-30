package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.yudao.module.system.dal.redis.auth.LoginUserRedisDAO;
import cn.iocoder.yudao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.addTime;

/**
 * 在线用户 Session Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class UserSessionServiceImpl implements UserSessionService {

    @Resource
    private SysUserSessionMapper userSessionMapper;

    @Resource
    private AdminUserService userService;
    @Resource
    private LoginLogService loginLogService;

    @Resource
    private LoginUserRedisDAO loginUserCoreRedisDAO;

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public PageResult<SysUserSessionDO> getUserSessionPage(UserSessionPageReqVO reqVO) {
        // 处理基于用户昵称的查询
        Collection<Long> userIds = null;
        if (StrUtil.isNotEmpty(reqVO.getUsername())) {
            userIds = convertSet(userService.getUsersByUsername(reqVO.getUsername()), AdminUserDO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return PageResult.empty();
            }
        }
        return userSessionMapper.selectPage(reqVO, userIds);
    }

    // TODO @芋艿：优化下该方法
    @Override
    public long clearSessionTimeout() {
        // 获取db里已经超时的用户列表
        List<SysUserSessionDO> sessionTimeoutDOS = userSessionMapper.selectListBySessionTimoutLt();
        Map<String, SysUserSessionDO> timeoutSessionDOMap = sessionTimeoutDOS
                .stream()
                .filter(sessionDO -> loginUserCoreRedisDAO.get(sessionDO.getId()) == null)
                .collect(Collectors.toMap(SysUserSessionDO::getId, o -> o));
        // 确认已经超时,按批次移出在线用户列表
        if (CollUtil.isNotEmpty(timeoutSessionDOMap)) {
            Lists.partition(new ArrayList<>(timeoutSessionDOMap.keySet()), 100)
                    .forEach(userSessionMapper::deleteBatchIds);
            // 记录用户超时退出日志
            createTimeoutLogoutLog(timeoutSessionDOMap.values());
        }
        return timeoutSessionDOMap.size();
    }

    private void createTimeoutLogoutLog(Collection<SysUserSessionDO> timeoutSessionDOS) {
        for (SysUserSessionDO timeoutSessionDO : timeoutSessionDOS) {
            LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
            reqDTO.setLogType(LoginLogTypeEnum.LOGOUT_TIMEOUT.getType());
            reqDTO.setTraceId(TracerUtils.getTraceId());
            reqDTO.setUserId(timeoutSessionDO.getUserId());
            reqDTO.setUserType(timeoutSessionDO.getUserType());
            reqDTO.setUsername(timeoutSessionDO.getUsername());
            reqDTO.setUserAgent(timeoutSessionDO.getUserAgent());
            reqDTO.setUserIp(timeoutSessionDO.getUserIp());
            reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
            loginLogService.createLoginLog(reqDTO);
        }
    }

    @Override
    public String createUserSession(LoginUser loginUser, String userIp, String userAgent) {
        // 生成 Session 编号
        String sessionId = generateSessionId();
        // 写入 Redis 缓存
        loginUser.setUpdateTime(new Date());
        loginUserCoreRedisDAO.set(sessionId, loginUser);
        // 写入 DB 中
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userType(loginUser.getUserType())
                .userIp(userIp).userAgent(userAgent).username(loginUser.getUsername())
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
        loginUserCoreRedisDAO.set(sessionId, loginUser);
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
        loginUserCoreRedisDAO.delete(sessionId);
        // 删除 DB 记录
        userSessionMapper.deleteById(sessionId);
    }

    @Override
    public LoginUser getLoginUser(String sessionId) {
        return loginUserCoreRedisDAO.get(sessionId);
    }

    @Override
    public Long getSessionTimeoutMillis() {
        return securityProperties.getSessionTimeout().toMillis();
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
