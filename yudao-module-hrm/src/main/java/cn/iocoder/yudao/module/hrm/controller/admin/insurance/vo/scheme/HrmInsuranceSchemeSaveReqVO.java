package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceSchemeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 社保方案保存 Request VO")
@Data
public class HrmInsuranceSchemeSaveReqVO {

    @Schema(description = "社保方案编号", example = "1024")
    private Long id;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "深圳标准社保方案")
    @NotBlank(message = "方案名称不能为空")
    @Size(max = 64, message = "方案名称不能超过 64 个字符")
    private String name;

    @Schema(description = "参保地区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "440300")
    @NotNull(message = "参保城市不能为空")
    private Integer areaId;

    @Schema(description = "户籍类型", example = "深户")
    @Size(max = 64, message = "户籍类型不能超过 64 个字符")
    private String householdType;

    @Schema(description = "方案类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "方案类型不能为空")
    @InEnum(value = HrmInsuranceSchemeTypeEnum.class, message = "方案类型必须是 {value}")
    private Integer type;

    @Schema(description = "社保项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "社保项目不能为空")
    private List<@NotNull(message = "社保项目不能为空") @Valid HrmInsuranceSchemeProjectSaveReqVO> projectList;

}
