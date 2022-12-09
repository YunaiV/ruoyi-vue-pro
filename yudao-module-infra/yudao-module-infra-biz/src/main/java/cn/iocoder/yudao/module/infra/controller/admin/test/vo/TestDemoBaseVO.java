package cn.iocoder.yudao.module.infra.controller.admin.test.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

/**
* 字典类型 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class TestDemoBaseVO {

    @Schema(title = "名字", required = true)
    @NotNull(message = "名字不能为空")
    private String name;

    @Schema(title = "状态", required = true)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(title = "类型", required = true)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(title = "分类", required = true)
    @NotNull(message = "分类不能为空")
    private Integer category;

    @Schema(title = "备注")
    private String remark;

}
