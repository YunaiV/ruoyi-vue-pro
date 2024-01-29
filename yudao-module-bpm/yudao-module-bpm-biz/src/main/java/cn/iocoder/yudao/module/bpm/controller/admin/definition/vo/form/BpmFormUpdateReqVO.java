package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;
import java.util.List;

@Schema(description = "管理后台 - 动态表单更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmFormUpdateReqVO extends BpmFormBaseVO {

    @Schema(description = "表单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "表单编号不能为空")
    private Long id;

    @Schema(description = "表单的配置-JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表单的配置不能为空")
    private String conf;

    @Schema(description = "表单项的数组-JSON 字符串的数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表单项的数组不能为空")
    private List<String> fields;

}
