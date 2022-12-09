package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;
import java.util.List;

@Schema(title = "管理后台 - 动态表单更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmFormUpdateReqVO extends BpmFormBaseVO {

    @Schema(title = "表单编号", required = true, example = "1024")
    @NotNull(message = "表单编号不能为空")
    private Long id;

    @Schema(title = "表单的配置", required = true, description = "JSON 字符串")
    @NotNull(message = "表单的配置不能为空")
    private String conf;

    @Schema(title = "表单项的数组", required = true, description = "JSON 字符串的数组")
    @NotNull(message = "表单项的数组不能为空")
    private List<String> fields;

}
