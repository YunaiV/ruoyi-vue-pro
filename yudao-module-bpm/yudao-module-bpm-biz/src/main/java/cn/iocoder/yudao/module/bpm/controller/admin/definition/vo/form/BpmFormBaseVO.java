package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

/**
* 动态表单 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class BpmFormBaseVO {

    @Schema(description = "表单名称", required = true, example = "芋道")
    @NotNull(message = "表单名称不能为空")
    private String name;

    @Schema(description = "表单状态-参见 CommonStatusEnum 枚举", required = true, example = "1")
    @NotNull(message = "表单状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "我是备注")
    private String remark;

}
