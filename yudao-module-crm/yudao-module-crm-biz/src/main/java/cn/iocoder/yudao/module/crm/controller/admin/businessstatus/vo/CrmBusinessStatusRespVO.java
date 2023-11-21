package cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "管理后台 - 商机状态 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessStatusRespVO extends CrmBusinessStatusBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6802")
    private Long id;

}
