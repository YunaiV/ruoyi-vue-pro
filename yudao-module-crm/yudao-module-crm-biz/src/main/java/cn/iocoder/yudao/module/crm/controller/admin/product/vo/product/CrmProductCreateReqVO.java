package cn.iocoder.yudao.module.crm.controller.admin.product.vo.product;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 产品创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmProductCreateReqVO extends CrmProductBaseVO {
    @Schema(description = "负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31926")
    @NotNull(message = "负责人的用户编号不能为空")
    private Long ownerUserId;
}
