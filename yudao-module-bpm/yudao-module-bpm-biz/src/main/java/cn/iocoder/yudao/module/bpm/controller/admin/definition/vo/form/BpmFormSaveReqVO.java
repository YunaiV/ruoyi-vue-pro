package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 动态表单创建/更新 Request VO")
@Data
public class BpmFormSaveReqVO {

    @Schema(description = "表单编号", example = "1024")
    private Long id;

    @Schema(description = "表单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    @NotNull(message = "表单名称不能为空")
    private String name;

    @Schema(description = "表单的配置-JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表单的配置不能为空")
    private String conf;

    @Schema(description = "表单项的数组-JSON 字符串的数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表单项的数组不能为空")
    private List<String> fields;

    @Schema(description = "表单状态-参见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "表单状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "我是备注")
    private String remark;

}
