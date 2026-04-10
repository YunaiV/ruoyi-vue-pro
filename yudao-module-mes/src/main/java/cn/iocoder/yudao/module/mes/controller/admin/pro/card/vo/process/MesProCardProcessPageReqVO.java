package cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 流转卡工序记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProCardProcessPageReqVO extends PageParam {

    @Schema(description = "流转卡编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "流转卡编号不能为空")
    private Long cardId;

    @Schema(description = "工序编号", example = "200")
    private Long processId;

    @Schema(description = "工位编号", example = "300")
    private Long workstationId;

    @Schema(description = "操作人编号", example = "400")
    private Long userId;

}
