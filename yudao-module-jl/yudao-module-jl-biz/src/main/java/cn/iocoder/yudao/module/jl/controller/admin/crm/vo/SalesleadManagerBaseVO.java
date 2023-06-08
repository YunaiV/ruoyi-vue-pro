package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 销售线索中的项目售前支持人员 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SalesleadManagerBaseVO {

    @Schema(description = "线索id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3822")
    @NotNull(message = "线索id不能为空")
    private Long salesleadId;

    @Schema(description = "销售售中人员", requiredMode = Schema.RequiredMode.REQUIRED, example = "11334")
    @NotNull(message = "销售售中人员不能为空")
    private Long managerId;

}
