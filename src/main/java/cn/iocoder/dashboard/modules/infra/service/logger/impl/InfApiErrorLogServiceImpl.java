package cn.iocoder.dashboard.modules.infra.service.logger.impl;

import cn.iocoder.dashboard.framework.logger.apilog.core.service.dto.ApiErrorLogCreateDTO;
import cn.iocoder.dashboard.modules.infra.service.logger.InfApiErrorLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * API 错误日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class InfApiErrorLogServiceImpl implements InfApiErrorLogService {

    @Override
    @Async
    public void createApiErrorLogAsync(ApiErrorLogCreateDTO createDTO) {

    }

}
