package cn.iocoder.yudao.module.infra.api.logger;

import cn.iocoder.yudao.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;

import javax.validation.Valid;

/**
 * API 错误日志的 API 接口
 *
 * @author 芋道源码
 */
public interface ApiErrorLogApi {

    /**
     * 创建 API 错误日志
     *
     * @param createDTO 创建信息
     */
    void createApiErrorLog(@Valid ApiErrorLogCreateReqDTO createDTO);

    /**
     * 清理 @param errorLogJobDay 天的访问日志
     *
     * @param errorLogJobDay 超过多少天就进行清理
     */
    void jobCleanErrorLog(Integer errorLogJobDay);
}
