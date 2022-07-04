package cn.iocoder.yudao.framework.operatelog.core.service;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 操作日志
 *
 * @author 芋道源码
 */
@Data
public class OperateLog {

    /**
     * 链路追踪编号
     */
    private String traceId;

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作名
     */
    private String name;

    /**
     * 操作分类
     */
    private Integer type;

    /**
     * 操作明细
     */
    private String content;

    /**
     * 拓展字段
     */
    private Map<String, Object> exts;

    /**
     * 请求方法名
     */
    private String requestMethod;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 浏览器 UserAgent
     */
    private String userAgent;

    /**
     * Java 方法名
     */
    private String javaMethod;

    /**
     * Java 方法的参数
     */
    private String javaMethodArgs;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 执行时长，单位：毫秒
     */
    private Integer duration;

    /**
     * 结果码
     */
    private Integer resultCode;

    /**
     * 结果提示
     */
    private String resultMsg;

    /**
     * 结果数据
     */
    private String resultData;

}
