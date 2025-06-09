package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TmsFirstMileRequestItemOffReqVO {
    @Schema(description = "ids")
    @NotNull(message = "ids不能为空")
    private List<Long> itemIds;

    @Schema(description = "开启、关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开启、关闭不能为空")
    private Boolean enable;
}
