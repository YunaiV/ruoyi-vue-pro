package cn.iocoder.yudao.module.crm.controller.admin.operatelog.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 跟进 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmOperateLogV2RespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long id;
    /**
     * 链路追踪编号
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private String traceId;
    /**
     * 用户编号
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;
    /**
     * 用户名称
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String userName;
    /**
     * 用户类型
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer userType;
    /**
     * 操作模块类型
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private String type;
    /**
     * 操作名
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "修改客户")
    private String subType;
    /**
     * 操作模块业务编号
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long bizId;
    /**
     * 操作内容
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "将什么从什么改为了什么")
    private String action;
    /**
     * 拓展字段
     */
    @Schema(description = "编号", example = "{orderId: 1}")
    private String extra;

    /**
     * 请求方法名
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private String requestMethod;
    /**
     * 请求地址
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private String requestUrl;
    /**
     * 用户 IP
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private String userIp;
    /**
     * 浏览器 UA
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private String userAgent;

    /**
     * 创建时间
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01")
    private LocalDateTime createTime;

}
