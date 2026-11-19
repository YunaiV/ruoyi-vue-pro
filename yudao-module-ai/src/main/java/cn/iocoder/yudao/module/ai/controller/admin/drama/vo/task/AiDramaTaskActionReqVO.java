package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧生成任务操作 Request VO：重新提交、同步状态、标记失败")
@Data
public class AiDramaTaskActionReqVO {

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "任务类型不能为空")
    @InEnum(AiDramaTaskTypeEnum.class)
    private Integer type;

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "记录编号不能为空")
    private Long id;

}
