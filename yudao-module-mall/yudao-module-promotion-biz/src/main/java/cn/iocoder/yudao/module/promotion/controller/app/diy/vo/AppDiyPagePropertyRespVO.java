package cn.iocoder.yudao.module.promotion.controller.app.diy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "用户 App - 装修页面属性 Response VO")
@Data
@ToString(callSuper = true)
public class AppDiyPagePropertyRespVO {

    @Schema(description = "装修页面编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    private Long id;

    @Schema(description = "页面名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String name;

    @Schema(description = "页面属性", example = "[]")
    private String property;

}
