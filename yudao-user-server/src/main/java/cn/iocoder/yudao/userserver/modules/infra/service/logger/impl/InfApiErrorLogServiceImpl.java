package cn.iocoder.yudao.userserver.modules.infra.service.logger.impl;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiErrorLogCreateDTO;
import cn.iocoder.yudao.userserver.modules.infra.service.logger.InfApiErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.Future;

/**
 * API 错误日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class InfApiErrorLogServiceImpl implements InfApiErrorLogService {

    @Override
    public Future<Boolean> createApiErrorLogAsync(ApiErrorLogCreateDTO createDTO) {
        log.debug("{}", createDTO);
        return new AsyncResult<>(true);
    }
}
