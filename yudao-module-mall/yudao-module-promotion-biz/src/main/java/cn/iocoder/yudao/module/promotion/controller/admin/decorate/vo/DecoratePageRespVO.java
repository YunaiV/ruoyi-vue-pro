package cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author jason
 */
@Schema(description = "管理后台 - 页面装修 Resp VO")
@Data
public class DecoratePageRespVO {
    @Schema(description = "页面类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;
    @Schema(description = "页面组件", requiredMode = Schema.RequiredMode.REQUIRED, example = "TODO")
    private List<ComponentRespVO> components;

    @Schema(description = "管理后台 - 页面组件 Resp VO")
    @Data
    public static class ComponentRespVO {
        @Schema(description = "组件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "首页广告")
        private String title;
        @Schema(description = "组件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "nav-menu")
        private String componentCode;
        @Schema(description = "组件的内容配置项", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "TODO")
        private Object config;
        @Schema(description = "组件的样式配置", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "TODO")
        private Object style;
        @Schema(description = "组件的具体数据", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "TODO")
        private Object data;
    }

}
