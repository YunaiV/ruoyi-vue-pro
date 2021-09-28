package cn.iocoder.yudao.userserver.modules.infra.service.logger.impl;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.yudao.userserver.modules.infra.convert.logger.InfApiAccessLogConvert;
import cn.iocoder.yudao.userserver.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import cn.iocoder.yudao.userserver.modules.infra.dal.mysql.logger.InfApiAccessLogMapper;
import cn.iocoder.yudao.userserver.modules.infra.service.logger.InfApiAccessLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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

    @Resource
    private InfApiAccessLogMapper apiAccessLogMapper;

    @Override
    public Future<Boolean> createApiAccessLogAsync(ApiAccessLogCreateDTO createDTO) {
        InfApiAccessLogDO apiAccessLog = InfApiAccessLogConvert.INSTANCE.convert(createDTO);
        int insert = apiAccessLogMapper.insert(apiAccessLog);
        return new AsyncResult<>(insert > 1);
    }

}
