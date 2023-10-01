package cn.iocoder.yudao.framework.apilog.core.service;

/**
 * API 错误日志 Framework Service 接口
 *
 * @author 芋道源码
 */
public interface ApiErrorLogFrameworkService {

    /**
     * 创建 API 错误日志
     *
     * @param apiErrorLog API 错误日志
     */
    void createApiErrorLog(ApiErrorLog apiErrorLog);

    /**
     * 清理 @param errorLogJobDay 天的访问日志
     *
     * @param errorLogJobDay 超过多少天就进行清理
     */
    void jobCleanErrorLog(Integer errorLogJobDay);
}
