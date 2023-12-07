package cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 页面装修的保存 Request VO ")
@Data
public class DecorateComponentSaveReqVO {

    @Schema(description = "页面 id ", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "页面 id 不能为空")
    @InEnum(DecoratePageEnum.class)
    private Integer page;

    @Schema(description = "组件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "nav-menu")
    @NotEmpty(message = "组件编码不能为空")
    private String code;

    @Schema(description = "组件对应值, json 字符串, 含内容配置，具体数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "组件值为空")
    private String value;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}
