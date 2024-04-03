package cn.iocoder.yudao.framework.apilog.core.service;

import cn.iocoder.yudao.module.infra.api.logger.ApiAccessLogApi;
import cn.iocoder.yudao.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

/**
 * API 访问日志 Framework Service 实现类
 *
 * 基于 {@link ApiAccessLogApi} 服务，记录访问日志
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class ApiAccessLogFrameworkServiceImpl implements ApiAccessLogFrameworkService {

    private final ApiAccessLogApi apiAccessLogApi;

    @Override
    @Async
    public void createApiAccessLog(ApiAccessLogCreateReqDTO reqDTO) {
        apiAccessLogApi.createApiAccessLog(reqDTO);
    }

}
