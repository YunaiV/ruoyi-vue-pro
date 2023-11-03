package cn.iocoder.yudao.module.promotion.controller.app.decorate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 页面组件 Resp VO")
@Data
public class AppDecorateComponentRespVO {

    @Schema(description = "组件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "nav-menu")
    private String code;

    @Schema(description = "组件的内容配置项", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "TODO")
    private String value;

}
