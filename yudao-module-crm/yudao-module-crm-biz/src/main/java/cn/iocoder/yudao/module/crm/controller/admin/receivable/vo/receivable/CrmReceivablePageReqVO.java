package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - CRM 回款分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmReceivablePageReqVO extends PageParam {

    @Schema(description = "回款编号")
    private String no;

    @Schema(description = "回款计划编号", example = "31177")
    private Long planId;

    @Schema(description = "客户编号", example = "4963")
    private Long customerId;

}
