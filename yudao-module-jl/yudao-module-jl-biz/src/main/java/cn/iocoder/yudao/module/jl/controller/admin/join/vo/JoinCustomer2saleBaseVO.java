package cn.iocoder.yudao.module.jl.controller.admin.join.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 客户所属的销售人员 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class JoinCustomer2saleBaseVO {

    @Schema(description = "客户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18599")
    @NotNull(message = "客户id不能为空")
    private Long customerId;

    @Schema(description = "销售 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30588")
    @NotNull(message = "销售 id不能为空")
    private Long salesId;

}
