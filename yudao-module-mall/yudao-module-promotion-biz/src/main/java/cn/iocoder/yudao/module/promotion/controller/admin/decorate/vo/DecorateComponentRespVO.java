package cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 页面装修 Resp VO")
@Data
public class DecorateComponentRespVO {

    @Schema(description = "组件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "nav-menu")
    private String code;

    @Schema(description = "组件的内容配置项", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "TODO")
    private String value;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}
