package cn.iocoder.yudao.module.system.api.logger.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * 操作日志创建 Request DTO
 */
@Data
public class OperateLogCreateReqDTO {

    /**
     * 链路追踪编号
     */
    private String traceId;

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 用户类型
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 操作模块
     */
    @NotEmpty(message = "操作模块不能为空")
    private String module;

    /**
     * 操作名
     */
    @NotEmpty(message = "操作名")
    private String name;

    /**
     * 操作分类
     */
    @NotNull(message = "操作分类不能为空")
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
    @NotEmpty(message = "请求方法名不能为空")
    private String requestMethod;

    /**
     * 请求地址
     */
    @NotEmpty(message = "请求地址不能为空")
    private String requestUrl;

    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    /**
     * 浏览器 UserAgent
     */
    @NotEmpty(message = "浏览器 UserAgent 不能为空")
    private String userAgent;

    /**
     * Java 方法名
     */
    @NotEmpty(message = "Java 方法名不能为空")
    private String javaMethod;

    /**
     * Java 方法的参数
     */
    private String javaMethodArgs;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private Date startTime;

    /**
     * 执行时长，单位：毫秒
     */
    @NotNull(message = "执行时长不能为空")
    private Integer duration;

    /**
     * 结果码
     */
    @NotNull(message = "结果码不能为空")
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
