package cn.iocoder.yudao.framework.apilog.core.service;

/**
 * API 访问日志 Framework Service 接口
 *
 * @author 芋道源码
 */
public interface ApiAccessLogFrameworkService {

    /**
     * 创建 API 访问日志
     *
     * @param apiAccessLog API 访问日志
     */
    void createApiAccessLog(ApiAccessLog apiAccessLog);

    /**
     * 清理 @param accessLogJobDay 天的访问日志
     *
     * @param accessLogJobDay 超过多少天就进行清理
     */
    void jobCleanAccessLog(Integer accessLogJobDay);
}
