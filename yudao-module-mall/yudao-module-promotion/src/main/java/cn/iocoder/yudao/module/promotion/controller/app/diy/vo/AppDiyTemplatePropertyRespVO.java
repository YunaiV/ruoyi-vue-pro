package cn.iocoder.yudao.module.promotion.controller.app.diy.vo;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "用户 App - 装修模板属性 Response VO")
@Data
@ToString(callSuper = true)
public class AppDiyTemplatePropertyRespVO {

    @Schema(description = "装修模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认主题")
    private String name;

    @Schema(description = "模板属性", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @JsonRawValue
    private String property;

    @Schema(description = "首页", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @JsonRawValue
    private String home;

    @Schema(description = "我的", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @JsonRawValue
    private String user;

}
