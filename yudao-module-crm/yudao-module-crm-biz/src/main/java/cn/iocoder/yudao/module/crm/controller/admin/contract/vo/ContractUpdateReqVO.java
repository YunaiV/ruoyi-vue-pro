package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "管理后台 - 合同更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContractUpdateReqVO extends ContractBaseVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "合同编号不能为空")
    private Long id;

    @Schema(description = "只读权限的用户编号数组")
    private Set<Long> roUserIds;

    @Schema(description = "读写权限的用户编号数组")
    private Set<Long> rwUserIds;

}
