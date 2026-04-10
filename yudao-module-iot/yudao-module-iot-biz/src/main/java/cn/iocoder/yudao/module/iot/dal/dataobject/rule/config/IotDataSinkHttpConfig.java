package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import lombok.Data;

import java.util.Map;

/**
 * IoT HTTP 配置 {@link IotAbstractDataSinkConfig} 实现类
 *
 * @author HUIHUI
 */
@Data
public class IotDataSinkHttpConfig extends IotAbstractDataSinkConfig {

    /**
     * 请求 URL
     */
    private String url;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求头
     */
    private Map<String, String> headers;
    /**
     * 请求参数
     */
    private Map<String, String> query;
    /**
     * 请求体
     */
    private String body;

}