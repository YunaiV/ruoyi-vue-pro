package cn.iocoder.yudao.module.promotion.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 装修模板 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class DiyTemplateBaseVO {

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认主题")
    @NotNull(message = "模板名称不能为空")
    private String name;

    @Schema(description = "备注", example = "默认主题")
    private String remark;

    @Schema(description = "预览图", example = "[https://www.iocoder.cn/1.jpg]")
    private List<String> previewImageUrls;

    @Schema(description = "模板属性，JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String property;

}
