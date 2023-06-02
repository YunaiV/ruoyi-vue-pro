package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 项目的实验名目保存 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectCategoryWithSupplyAndChargeItemVO extends ProjectCategoryBaseVO {

    @Schema(description = "收费项", example = "[]")
    private List<ProjectChargeitemSubClass> chargeItemList;

    @Schema(description = "物资项", example = "[]")
    private List<ProjectSupplySubClass> supplyList;
}
