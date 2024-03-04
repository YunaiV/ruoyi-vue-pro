package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户客户统计响应 Base VO
 */
@Data
public class CrmStatisticsCustomerByUserBaseRespVO {

    @Schema(description = "负责人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long ownerUserId;

    @Schema(description = "负责人", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String ownerUserName;

}
