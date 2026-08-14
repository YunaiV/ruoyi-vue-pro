package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工档案字段配置保存 Request VO")
@Data
public class HrmEmployeeArchiveFieldConfigSaveReqVO {

    @Schema(description = "字段配置列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "字段配置列表不能为空")
    @DiffLogField(name = "字段配置")
    private List<HrmEmployeeFieldConfigSaveReqVO> fields;

}
