package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.UserSessionDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.UserSessionMapper;
import cn.iocoder.yudao.module.system.dal.redis.auth.LoginUserRedisDAO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
    private UserSessionMapper userSessionMapper;

    @Resource
    private AdminUserService userService;
    @Resource
    private LoginLogService loginLogService;

    @Resource
    private LoginUserRedisDAO loginUserRedisDAO;

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public PageResult<UserSessionDO> getUserSessionPage(UserSessionPageReqVO reqVO) {
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

    @Override
    public long deleteTimeoutSession() {
        // 获取 db 里已经超时的用户列表
        List<UserSessionDO> timeoutSessions = userSessionMapper.selectListBySessionTimoutLt();
        if (CollUtil.isEmpty(timeoutSessions)) {
            return 0L;
        }

        // 由于过期的用户一般不多，所以顺序遍历，进行清理
        int count = 0;
        for (UserSessionDO session : timeoutSessions) {
            // 基于 Redis 二次判断，同时也保证 Redis Key 的立即过期，避免延迟导致浪费内存空间
            if (loginUserRedisDAO.exists(session.getToken())) {
                continue;
            }
            userSessionMapper.deleteById(session.getId());
            // 记录退出日志
            createLogoutLog(session, LoginLogTypeEnum.LOGOUT_TIMEOUT);
            count++;
        }
        return count;
    }

    private void createLogoutLog(UserSessionDO session, LoginLogTypeEnum type) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(type.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(session.getUserId());
        reqDTO.setUserType(session.getUserType());
        reqDTO.setUsername(session.getUsername());
        reqDTO.setUserAgent(session.getUserAgent());
        reqDTO.setUserIp(session.getUserIp());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    @Override
    public String createUserSession(LoginUser loginUser, String userIp, String userAgent) {
        // 生成 Session 编号
        String token = generateToken();
        // 写入 Redis 缓存
        loginUser.setUpdateTime(new Date());
        loginUserRedisDAO.set(token, loginUser);
        // 写入 DB 中
        UserSessionDO userSession = UserSessionDO.builder().token(token)
                .userId(loginUser.getId()).userType(loginUser.getUserType())
                .userIp(userIp).userAgent(userAgent).username(loginUser.getUsername())
                .sessionTimeout(addTime(Duration.ofMillis(getSessionTimeoutMillis())))
                .build();
        userSessionMapper.insert(userSession);
        // 返回 Token 令牌
        return token;
    }

    @Override
    public void refreshUserSession(String token, LoginUser loginUser) {
        // 写入 Redis 缓存
        loginUser.setUpdateTime(new Date());
        loginUserRedisDAO.set(token, loginUser);
        // 更新 DB 中
        UserSessionDO updateObj = UserSessionDO.builder().build();
        updateObj.setUsername(loginUser.getUsername());
        updateObj.setUpdateTime(new Date());
        updateObj.setSessionTimeout(addTime(Duration.ofMillis(getSessionTimeoutMillis())));
        userSessionMapper.updateByToken(token, updateObj);
    }

    @Override
    public void deleteUserSession(String token) {
        // 删除 Redis 缓存
        loginUserRedisDAO.delete(token);
        // 删除 DB 记录
        userSessionMapper.deleteByToken(token);
        // 无需记录日志，因为退出那已经记录
    }

    @Override
    public void deleteUserSession(Long id) {
        UserSessionDO session = userSessionMapper.selectById(id);
        if (session == null) {
            return;
        }
        // 删除 Redis 缓存
        loginUserRedisDAO.delete(session.getToken());
        // 删除 DB 记录
        userSessionMapper.deleteById(id);
        // 记录退出日志
        createLogoutLog(session, LoginLogTypeEnum.LOGOUT_DELETE);
    }

    @Override
    public LoginUser getLoginUser(String token) {
        return loginUserRedisDAO.get(token);
    }

    @Override
    public Long getSessionTimeoutMillis() {
        return securityProperties.getSessionTimeout().toMillis();
    }

    /**
     * 生成 Token 令牌，目前采用 UUID 算法
     *
     * @return Session 编号
     */
    private static String generateToken() {
        return IdUtil.fastSimpleUUID();
    }

}
