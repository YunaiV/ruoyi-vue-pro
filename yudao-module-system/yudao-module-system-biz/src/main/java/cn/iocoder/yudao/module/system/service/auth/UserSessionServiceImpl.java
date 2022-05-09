package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
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
import java.util.Collection;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

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

}
