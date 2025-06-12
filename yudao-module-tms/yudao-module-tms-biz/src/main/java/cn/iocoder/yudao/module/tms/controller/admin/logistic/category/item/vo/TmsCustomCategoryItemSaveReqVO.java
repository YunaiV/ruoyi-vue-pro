package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 海关分类子表新增/修改 Request VO")
@Data
public class TmsCustomCategoryItemSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = Validation.OnCreate.class, message = "创建时，子项id必须为空")
//    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，子项id不能为空")
    private Long id;

    @Schema(description = "国家-字典", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "国家-字典不能为空")
    private Integer countryCode;

    @Schema(description = "HS编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "HS编码不能为空")
    private String hscode;

    @Schema(description = "税率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "税率不能为空")
    private BigDecimal taxRate;

}