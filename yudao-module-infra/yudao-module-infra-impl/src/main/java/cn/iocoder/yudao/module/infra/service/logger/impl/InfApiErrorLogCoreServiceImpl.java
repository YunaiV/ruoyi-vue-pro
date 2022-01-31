package cn.iocoder.yudao.module.infra.service.logger.impl;

import cn.iocoder.yudao.coreservice.modules.infra.convert.logger.InfApiErrorLogCoreConvert;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.logger.InfApiErrorLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.infra.enums.logger.InfApiErrorLogProcessStatusEnum;
import cn.iocoder.yudao.coreservice.modules.infra.service.logger.InfApiErrorLogCoreService;
import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiErrorLogCreateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * API 错误日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class InfApiErrorLogCoreServiceImpl implements InfApiErrorLogCoreService {

    @Resource
    private InfApiErrorLogCoreMapper apiErrorLogMapper;

    @Override
    @Async
    public void createApiErrorLogAsync(ApiErrorLogCreateReqDTO createDTO) {
        InfApiErrorLogDO apiErrorLog = InfApiErrorLogCoreConvert.INSTANCE.convert(createDTO);
        apiErrorLog.setProcessStatus(InfApiErrorLogProcessStatusEnum.INIT.getStatus());
        apiErrorLogMapper.insert(apiErrorLog);
    }

}
