package cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 装修模板 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DiyTemplateRespVO extends DiyTemplateBaseVO {

    @Schema(description = "装修模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "是否使用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean used;

    @Schema(description = "使用时间", example = "使用时间")
    private LocalDateTime usedTime;

}
