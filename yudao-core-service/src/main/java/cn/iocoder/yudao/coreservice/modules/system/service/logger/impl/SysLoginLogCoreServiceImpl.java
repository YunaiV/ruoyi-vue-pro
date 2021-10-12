package cn.iocoder.yudao.coreservice.modules.system.service.logger.impl;

import cn.iocoder.yudao.coreservice.modules.system.convert.logger.SysLoginLogCoreConvert;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.logger.SysLoginLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.SysLoginLogCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 登录日志 Service Core 实现
 *
 * @author 芋道源码
 */
@Service
public class SysLoginLogCoreServiceImpl implements SysLoginLogCoreService {

    @Resource
    private SysLoginLogCoreMapper loginLogMapper;

    @Override
    public void createLoginLog(SysLoginLogCreateReqDTO reqDTO) {
        SysLoginLogDO loginLog = SysLoginLogCoreConvert.INSTANCE.convert(reqDTO);
        // 插入
        loginLogMapper.insert(loginLog);
    }

}
