package cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 装修模板属性更新 Request VO")
@Data
@ToString(callSuper = true)
public class DiyTemplatePropertyUpdateRequestVO {

    @Schema(description = "装修模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    @NotNull(message = "装修模板编号不能为空")
    private Long id;

    @Schema(description = "模板属性，JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @NotBlank(message = "模板属性不能为空")
    private String property;

}
