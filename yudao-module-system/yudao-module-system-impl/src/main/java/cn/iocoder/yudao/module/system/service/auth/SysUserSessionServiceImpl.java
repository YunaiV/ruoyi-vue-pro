package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.yudao.module.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.yudao.module.system.service.auth.SysUserSessionService;
import cn.iocoder.yudao.module.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.redis.auth.SysLoginUserCoreRedisDAO;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.SysLoginLogCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 在线用户 Session Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class SysUserSessionServiceImpl implements SysUserSessionService {

    @Resource
    private SysUserSessionMapper userSessionMapper;
    @Resource
    private SysUserService userService;
    @Resource
    private SysLoginLogCoreService loginLogCoreService;

    @Resource
    private SysLoginUserCoreRedisDAO loginUserCoreRedisDAO;

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
            SysLoginLogCreateReqDTO reqDTO = new SysLoginLogCreateReqDTO();
            reqDTO.setLogType(SysLoginLogTypeEnum.LOGOUT_TIMEOUT.getType());
            reqDTO.setTraceId(TracerUtils.getTraceId());
            reqDTO.setUserId(timeoutSessionDO.getUserId());
            reqDTO.setUserType(timeoutSessionDO.getUserType());
            reqDTO.setUsername(timeoutSessionDO.getUsername());
            reqDTO.setUserAgent(timeoutSessionDO.getUserAgent());
            reqDTO.setUserIp(timeoutSessionDO.getUserIp());
            reqDTO.setResult(SysLoginResultEnum.SUCCESS.getResult());
            loginLogCoreService.createLoginLog(reqDTO);
        }
    }

}
