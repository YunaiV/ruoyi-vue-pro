package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import lombok.Data;

import java.util.Map;

/**
 * HTTP 配置
 *
 * @author HUIHUI
 */
@Data
public class IotDataBridgeHttpConfig extends IotDataBridgeAbstractConfig {

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