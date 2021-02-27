package cn.iocoder.dashboard.framework.logger.apilog.core.service;

import cn.iocoder.dashboard.framework.logger.apilog.core.service.dto.ApiErrorLogCreateDTO;

import javax.validation.Valid;

/**
 * API 错误日志 Framework Service 接口
 *
 * @author 芋道源码
 */
public interface ApiErrorLogFrameworkService {

    /**
     * 创建 API 错误日志
     *
     * @param createDTO 创建信息
     */
    void createApiErrorLogAsync(@Valid ApiErrorLogCreateDTO createDTO);

}
