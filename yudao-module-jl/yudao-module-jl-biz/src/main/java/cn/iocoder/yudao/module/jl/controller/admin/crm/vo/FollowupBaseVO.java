package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

/**
 * 销售线索跟进，可以是跟进客户，也可以是跟进线索 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class FollowupBaseVO{

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "内容不能为空")
    private String content;

    @Schema(description = "客户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24011")
    @NotNull(message = "客户id不能为空")
    private Long customerId;

    @Schema(description = "跟进实体的 id，项目、线索、款项，客户等", example = "29426")
    private Long refId;

    @Schema(description = "跟进类型：日常联系、销售线索、催款等", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String type;
}
