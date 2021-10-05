package cn.iocoder.yudao.adminserver.modules.system.service.logger.impl;

import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog.SysLoginLogExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog.SysLoginLogPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.convert.logger.SysLoginLogConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.logger.SysLoginLogMapper;
import cn.iocoder.yudao.adminserver.modules.system.service.logger.SysLoginLogService;
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
        // 获得用户
        SysUserDO user = userService.getUserByUsername(reqVO.getUsername());
        if (user != null) {
            loginLog.setUserId(user.getId());
        }
        loginLog.setUserType(UserTypeEnum.ADMIN.getValue());
        // 插入
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
