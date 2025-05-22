package cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 装修页面属性更新 Request VO")
@Data
@ToString(callSuper = true)
public class DiyPagePropertyUpdateRequestVO {

    @Schema(description = "装修页面编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    @NotNull(message = "装修页面编号不能为空")
    private Long id;

    @Schema(description = "页面属性，JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @NotBlank(message = "页面属性不能为空")
    private String property;

}
