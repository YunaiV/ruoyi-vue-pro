package cn.iocoder.yudao.module.mp.controller.admin.message.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 公众号模版消息 Response VO")
@Data
public class MpMessageTemplateRespVO {

    @Schema(description = "模版主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7019")
    private Long id;

    @Schema(description = "公众号账号的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountId;

    @Schema(description = "appId", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx1234567890abcdef")
    private String appId;

    @Schema(description = "公众号模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "IjkGxO9M_mC9pE5Yl7QYJk1h0Dj2N4lC3oOp6rRsT8u")
    private String templateId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单状态提醒")
    private String title;

    @Schema(description = "模板内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "模板示例")
    private String example;

    @Schema(description = "模板所属行业的一级行业", example = "电商")
    private String primaryIndustry;

    @Schema(description = "模板所属行业的二级行业", example = "商品售后")
    private String deputyIndustry;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}