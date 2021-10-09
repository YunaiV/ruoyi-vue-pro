package cn.iocoder.yudao.userserver.modules.system.service.logger.impl;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.userserver.modules.system.convert.logger.SysLoginLogConvert;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.logger.SysLoginLogMapper;
import cn.iocoder.yudao.userserver.modules.system.dal.mysql.logger.SysLoginLogDO;
import cn.iocoder.yudao.userserver.modules.system.service.logger.SysLoginLogService;
import cn.iocoder.yudao.userserver.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 登录日志 Service 实现
 */
@Service
public class SysLoginLogServiceImpl implements SysLoginLogService {

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Override
    public void createLoginLog(SysLoginLogCreateReqDTO reqDTO) {
        SysLoginLogDO loginLog = SysLoginLogConvert.INSTANCE.convert(reqDTO);
        loginLog.setUserType(UserTypeEnum.MEMBER.getValue());
        // 插入
        loginLogMapper.insert(loginLog);
    }

}
