package cn.iocoder.dashboard.modules.system.service.logger.impl;

import cn.iocoder.dashboard.framework.logger.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.dashboard.modules.system.service.logger.SysApiAccessLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * API 访问日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SysApiAccessLogServiceImpl implements SysApiAccessLogService {



    @Override
    public void createApiAccessLogAsync(@Valid ApiAccessLogCreateDTO createDTO) {

    }

}
