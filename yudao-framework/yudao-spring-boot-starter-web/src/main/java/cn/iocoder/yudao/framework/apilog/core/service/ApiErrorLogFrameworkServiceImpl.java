package cn.iocoder.yudao.framework.apilog.core.service;

import cn.iocoder.yudao.module.infra.api.logger.ApiErrorLogApi;
import cn.iocoder.yudao.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

/**
 * API 错误日志 Framework Service 实现类
 *
 * 基于 {@link ApiErrorLogApi} 服务，记录错误日志
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class ApiErrorLogFrameworkServiceImpl implements ApiErrorLogFrameworkService {

    private final ApiErrorLogApi apiErrorLogApi;

    @Override
    @Async
    public void createApiErrorLog(ApiErrorLogCreateReqDTO reqDTO) {
        try {
            apiErrorLogApi.createApiErrorLog(reqDTO);
        } catch (Throwable ex) {
            // 由于 @Async 异步调用，这里打印下日志，更容易跟进
            log.error("[createApiErrorLog][url({}) log({}) 发生异常]", reqDTO.getRequestUrl(), reqDTO, ex);
        }
    }

}
