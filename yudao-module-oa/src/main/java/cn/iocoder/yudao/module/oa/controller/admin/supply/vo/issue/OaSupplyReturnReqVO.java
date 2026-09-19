package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "管理后台 - 用品归还 Request VO")
@Data
public class OaSupplyReturnReqVO {

    @Schema(description = "明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "明细编号不能为空")
    private Long id;

    @Schema(description = "本次归还数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "本次归还数量不能为空")
    @Min(value = 1, message = "本次归还数量不能小于 {value}")
    private Integer quantity;

    @Schema(description = "归还备注", example = "物品完好，已归还入库")
    @Size(max = 500, message = "归还备注长度不能超过 {max} 个字符")
    private String returnRemark;

}
