package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户更新跟进信息 Request VO")
@Data
@ToString(callSuper = true)
public class CustomerUpdateFollowupVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28406")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "最近跟进记录", example = "最近跟进记录")
    private LocalDateTime lastFollowupTime;

    @Schema(description = "最近一次跟进 id")
    private Long lastFollowupId;
}
