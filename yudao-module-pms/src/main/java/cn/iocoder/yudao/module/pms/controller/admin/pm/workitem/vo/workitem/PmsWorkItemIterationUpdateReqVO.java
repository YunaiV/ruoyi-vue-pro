package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - PMS 工作项所属迭代更新 Request VO")
@Data
@Accessors(chain = true)
public class PmsWorkItemIterationUpdateReqVO {

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "工作项编号不能为空")
    private Long id;

    @Schema(description = "迭代编号；为空表示移回待规划", example = "2048")
    private Long iterationId;

}
