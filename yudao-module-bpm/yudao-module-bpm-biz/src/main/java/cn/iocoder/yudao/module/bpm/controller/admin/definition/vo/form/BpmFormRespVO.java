package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 动态表单 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmFormRespVO extends BpmFormBaseVO {

    @Schema(description = "表单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "表单的配置-JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表单的配置不能为空")
    private String conf;

    @Schema(description = "表单项的数组-JSON 字符串的数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表单项的数组不能为空")
    private List<String> fields;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
