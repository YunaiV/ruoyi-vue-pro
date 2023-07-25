package cn.iocoder.yudao.module.promotion.controller.app.decorate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 页面装修 Resp VO")
@Data
public class AppDecorateComponentRespVO {

    @Schema(description = "页面 id ", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer pageId;

    @Schema(description = "页面组件", requiredMode = Schema.RequiredMode.REQUIRED, example = "TODO")
    private List<AppComponentRespVO> components;

    @Schema(description = "用户 App - 页面组件 Resp VO")
    @Data
    public static class AppComponentRespVO {

        @Schema(description = "组件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "nav-menu")
        private String code;

        @Schema(description = "组件的内容配置项", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "TODO")
        private String value;

    }

}
