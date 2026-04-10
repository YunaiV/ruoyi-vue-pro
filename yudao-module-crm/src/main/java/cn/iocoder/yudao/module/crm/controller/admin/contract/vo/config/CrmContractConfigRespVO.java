package cn.iocoder.yudao.module.crm.controller.admin.contract.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 合同配置 Response VO")
@Data
public class CrmContractConfigRespVO {

    @Schema(description = "是否开启提前提醒", example = "true")
    private Boolean notifyEnabled;

    @Schema(description = "提前提醒天数", example = "2")
    private Integer notifyDays;

}
