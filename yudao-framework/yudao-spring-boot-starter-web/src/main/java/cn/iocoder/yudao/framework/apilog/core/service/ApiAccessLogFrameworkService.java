package cn.iocoder.yudao.framework.apilog.core.service;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiAccessLogCreateReqDTO;

import javax.validation.Valid;

/**
 * API 访问日志 Framework Service 接口
 *
 * @author 芋道源码
 */
public interface ApiAccessLogFrameworkService {

    /**
     * 创建 API 访问日志
     *
     * @param createDTO 创建信息
     */
    void createApiAccessLogAsync(@Valid ApiAccessLogCreateReqDTO createDTO);

}
