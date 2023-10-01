package cn.iocoder.yudao.module.infra.api.logger;

import cn.iocoder.yudao.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;

import javax.validation.Valid;

/**
 * API 访问日志的 API 接口
 *
 * @author 芋道源码
 */
public interface ApiAccessLogApi {

    /**
     * 创建 API 访问日志
     *
     * @param createDTO 创建信息
     */
    void createApiAccessLog(@Valid ApiAccessLogCreateReqDTO createDTO);

    // TODO @j-sentinel：这个我们先提供接口在 API，而是 infra 模块自己清理先哈；
    /**
     * 清理 @param accessLogJobDay 天的访问日志
     *
     * @param accessLogJobDay 超过多少天就进行清理
     */
    void jobCleanAccessLog(Integer accessLogJobDay);

}
