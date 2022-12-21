package cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class OperateLogBaseVO {

    @Schema(description = "链路追踪编号", required = true, example = "89aca178-a370-411c-ae02-3f0d672be4ab")
    @NotEmpty(message = "链路追踪编号不能为空")
    private String traceId;

    @Schema(description = "用户编号", required = true, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "操作模块", required = true, example = "订单")
    @NotEmpty(message = "操作模块不能为空")
    private String module;

    @Schema(description = "操作名", required = true, example = "创建订单")
    @NotEmpty(message = "操作名")
    private String name;

    @Schema(description = "操作分类,参见 OperateLogTypeEnum 枚举类", required = true, example = "1")
    @NotNull(message = "操作分类不能为空")
    private Integer type;

    @Schema(description = "操作明细", example = "修改编号为 1 的用户信息，将性别从男改成女，将姓名从芋道改成源码。")
    private String content;

    @Schema(description = "拓展字段", example = "{'orderId': 1}")
    private Map<String, Object> exts;

    @Schema(description = "请求方法名", required = true, example = "GET")
    @NotEmpty(message = "请求方法名不能为空")
    private String requestMethod;

    @Schema(description = "请求地址", required = true, example = "/xxx/yyy")
    @NotEmpty(message = "请求地址不能为空")
    private String requestUrl;

    @Schema(description = "用户 IP", required = true, example = "127.0.0.1")
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    @Schema(description = "浏览器 UserAgent", required = true, example = "Mozilla/5.0")
    @NotEmpty(message = "浏览器 UserAgent 不能为空")
    private String userAgent;

    @Schema(description = "Java 方法名", required = true, example = "cn.iocoder.yudao.adminserver.UserController.save(...)")
    @NotEmpty(message = "Java 方法名不能为空")
    private String javaMethod;

    @Schema(description = "Java 方法的参数")
    private String javaMethodArgs;

    @Schema(description = "开始时间", required = true)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "执行时长，单位：毫秒", required = true)
    @NotNull(message = "执行时长不能为空")
    private Integer duration;

    @Schema(description = "结果码", required = true)
    @NotNull(message = "结果码不能为空")
    private Integer resultCode;

    @Schema(description = "结果提示")
    private String resultMsg;

    @Schema(description = "结果数据")
    private String resultData;

}
