package cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 预算配置分页 Request VO")
@Data
public class AiBudgetConfigPageReqVO extends PageParam {

    @Schema(description = "用户编号，0 表示租户级", example = "0")
    private Long userId;

    @Schema(description = "周期类型", example = "MONTHLY")
    private String periodType;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
