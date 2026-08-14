package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.file;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeFileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工材料附件保存 Request VO")
@Data
public class HrmEmployeeFileSaveReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "附件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "附件类型不能为空")
    @InEnum(value = HrmEmployeeFileTypeEnum.class, message = "附件类型必须是 {value}")
    private Integer type;

    @Schema(description = "附件地址数组", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[https://example.com/archive.pdf]")
    @NotNull(message = "附件地址数组不能为空")
    @Size(max = 20, message = "同类附件不能超过 20 个")
    private List<@NotBlank(message = "附件地址不能为空")
            @Size(max = 512, message = "附件地址长度不能超过 512 个字符") String> fileUrls;

}
