package cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author jason
 */
@Schema(description = "管理后台 - 页面装修 Request VO ")
@Data
public class DecorateComponentReqVO {
    @Schema(description = "页面类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "页面类型不能为空")
    @InEnum(DecoratePageTypeEnum.class)
    private Integer type;
    @Schema(description = "页面组件列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "TODO")
    @NotEmpty(message = "页面组件列表不能为空")
    @Valid
    private List<ComponentReqVO> components;

    @Schema(description = "管理后台 - 页面装修组件 Request VO")
    @Data
    public static class ComponentReqVO {
        @Schema(description = "组件编码",  example = "1")
        private Long id;

        @Schema(description = "组件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "nav-menu")
        @NotEmpty(message = "组件编码不能为空")
        private String componentCode;

        @Schema(description = "组件对应值, json 字符串, 含内容配置，具体数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "TODO")
        @NotEmpty(message = "组件值为空")
        private String componentValue;
    }

}
