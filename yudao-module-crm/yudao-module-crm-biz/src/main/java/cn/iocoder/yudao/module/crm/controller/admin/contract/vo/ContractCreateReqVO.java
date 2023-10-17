package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 合同创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContractCreateReqVO extends ContractBaseVO {

    @Schema(description = "只读权限的用户编号数组")
    private String roUserIds;

    @Schema(description = "读写权限的用户编号数组")
    private String rwUserIds;

}
