package cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 装修页面 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class DiyPageBaseVO {

    @Schema(description = "装修模板编号", example = "26179")
    private Long templateId;

    @Schema(description = "页面名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotNull(message = "页面名称不能为空")
    private String name;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "预览图")
    private List<String> previewPicUrls;

}
