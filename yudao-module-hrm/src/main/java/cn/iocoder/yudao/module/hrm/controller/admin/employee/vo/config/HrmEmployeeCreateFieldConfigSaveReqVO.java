package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Schema(description = "管理后台 - HRM 新建员工字段配置保存 Request VO")
@Data
public class HrmEmployeeCreateFieldConfigSaveReqVO {

    @Schema(description = "入职状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "入职状态不能为空")
    @InEnum(value = HrmEmployeeEntryStatusEnum.class, message = "入职状态必须是 {value}")
    @Range(min = 1, max = 2, message = "入职状态必须是 1 或 2")
    @DiffLogField(name = "入职状态")
    private Integer entryStatus;

    @Schema(description = "字段配置列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "字段配置列表不能为空")
    @DiffLogField(name = "字段配置")
    private List<HrmEmployeeFieldConfigSaveReqVO> fields;

}
