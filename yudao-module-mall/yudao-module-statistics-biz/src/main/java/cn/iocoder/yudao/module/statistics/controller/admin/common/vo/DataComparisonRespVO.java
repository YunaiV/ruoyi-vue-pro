package cn.iocoder.yudao.module.statistics.controller.admin.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 数据对照 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataComparisonRespVO<T> {

    @Schema(description = "当前数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private T value;

    @Schema(description = "参照数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private T reference;

}
