package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.business.CrmBusinessEndStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - CRM 商机更新状态 Request VO")
@Data
public class CrmBusinessUpdateStatusReqVO {

    @Schema(description = "商机编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32129")
    @NotNull(message = "商机编号不能为空")
    private Long id;

    @Schema(description = "状态编号", example = "1")
    private Long statusId;

    @Schema(description = "结束状态", example = "1")
    @InEnum(value = CrmBusinessEndStatusEnum.class)
    private Integer endStatus;

    @AssertTrue(message = "变更状态不正确")
    public boolean isStatusValid() {
        return statusId != null || endStatus != null;
    }

}
