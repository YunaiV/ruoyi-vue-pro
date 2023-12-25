package cn.iocoder.yudao.module.system.api.logger.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统操作日志 Resp DTO
 *
 * @author HUIHUI
 */
@Data
public class OperateLogV2RespDTO {

    /**
     * 链路追踪编号
     */
    private String traceId;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 操作模块类型
     */
    private String type;
    /**
     * 操作名
     */
    private String subType;
    /**
     * 操作模块业务编号
     */
    private Long bizId;
    /**
     * 操作内容
     */
    private String content;
    /**
     * 拓展字段
     */
    private Map<String, Object> extra;

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
     * 浏览器 UA
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
    private LocalDateTime startTime;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
