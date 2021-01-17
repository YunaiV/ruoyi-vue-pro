package cn.iocoder.dashboard.modules.system.service.logger.impl;

import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysLoginLogConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.logger.SysLoginLogMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysLoginLogDO;
import cn.iocoder.dashboard.modules.system.service.logger.SysLoginLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 登陆日志 Service 实现
 */
@Service
public class SysLoginLogServiceImpl implements SysLoginLogService {

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Override
    public void createLoginLog(SysLoginLogCreateReqVO reqVO) {
        SysLoginLogDO loginLog = SysLoginLogConvert.INSTANCE.convert(reqVO);
        loginLogMapper.insert(loginLog);
    }

}
