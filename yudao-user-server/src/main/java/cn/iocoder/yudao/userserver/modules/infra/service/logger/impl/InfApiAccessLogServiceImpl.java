package cn.iocoder.yudao.userserver.modules.infra.service.logger.impl;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.yudao.userserver.modules.infra.service.logger.InfApiAccessLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.Future;

/**
 * API 访问日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class InfApiAccessLogServiceImpl implements InfApiAccessLogService {

    @Override
    public Future<Boolean> createApiAccessLogAsync(ApiAccessLogCreateDTO createDTO) {
        log.debug("{}", createDTO);
        return new AsyncResult<>(true);
    }
}
