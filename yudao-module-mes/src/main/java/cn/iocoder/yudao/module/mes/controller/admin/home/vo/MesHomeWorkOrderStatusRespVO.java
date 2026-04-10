package cn.iocoder.yudao.module.mes.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - MES 首页工单状态分布 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesHomeWorkOrderStatusRespVO {

    @Schema(description = "状态值", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "状态名", requiredMode = Schema.RequiredMode.REQUIRED, example = "草稿")
    private String statusName;

    @Schema(description = "工单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long count;

}
