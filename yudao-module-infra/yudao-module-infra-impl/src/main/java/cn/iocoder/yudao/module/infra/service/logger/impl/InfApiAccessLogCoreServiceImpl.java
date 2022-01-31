package cn.iocoder.yudao.module.infra.service.logger.impl;

import cn.iocoder.yudao.coreservice.modules.infra.convert.logger.InfApiAccessLogCoreConvert;
import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiAccessLogCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.logger.InfApiAccessLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.infra.service.logger.InfApiAccessLogCoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * API 访问日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class InfApiAccessLogCoreServiceImpl implements InfApiAccessLogCoreService {

    @Resource
    private InfApiAccessLogCoreMapper apiAccessLogMapper;

    @Override
    @Async
    public void createApiAccessLogAsync(ApiAccessLogCreateReqDTO createDTO) {
        InfApiAccessLogDO apiAccessLog = InfApiAccessLogCoreConvert.INSTANCE.convert(createDTO);
        apiAccessLogMapper.insert(apiAccessLog);
    }

}
