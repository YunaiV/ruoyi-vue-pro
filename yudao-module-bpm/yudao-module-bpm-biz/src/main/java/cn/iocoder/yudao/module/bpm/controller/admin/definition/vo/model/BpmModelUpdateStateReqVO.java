package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 流程模型更新状态 Request VO")
@Data
public class BpmModelUpdateStateReqVO {

    @Schema(description = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private String id;

    @Schema(description = "状态-见 SuspensionState 枚举", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer state;

}
