package cn.iocoder.dashboard.modules.system.service.logger.impl;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogPageReqVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysLoginLogConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.logger.SysLoginLogMapper;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.dashboard.modules.system.service.logger.SysLoginLogService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 登陆日志 Service 实现
 */
@Service
public class SysLoginLogServiceImpl implements SysLoginLogService {

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Resource
    private SysUserService userService;

    @Override
    public void createLoginLog(SysLoginLogCreateReqVO reqVO) {
        SysLoginLogDO loginLog = SysLoginLogConvert.INSTANCE.convert(reqVO);
        loginLogMapper.insert(loginLog);
    }

    @Override
    public PageResult<SysLoginLogDO> getLoginLogPage(SysLoginLogPageReqVO reqVO) {
        return loginLogMapper.selectPage(reqVO);
    }

    @Override
    public List<SysLoginLogDO> getLoginLogList(SysLoginLogExportReqVO reqVO) {
        return loginLogMapper.selectList(reqVO);
    }

}
