package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contact;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工联系人保存 Request VO")
@Data
public class HrmEmployeeContactSaveReqVO {

    @Schema(description = "联系人编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "联系人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotBlank(message = "联系人名称不能为空")
    @Size(max = 64, message = "联系人名称长度不能超过 64 个字符")
    @DiffLogField(name = "联系人名称")
    private String name;

    @Schema(description = "关系", example = "配偶")
    @Size(max = 64, message = "关系长度不能超过 64 个字符")
    @DiffLogField(name = "关系")
    private String relation;

    @Schema(description = "联系人电话", example = "15601691301")
    @Size(max = 40, message = "联系人电话长度不能超过 40 个字符")
    @DiffLogField(name = "联系人电话")
    private String phone;

    @Schema(description = "联系人工作单位", example = "示例科技")
    @Size(max = 128, message = "联系人工作单位长度不能超过 128 个字符")
    @DiffLogField(name = "联系人工作单位")
    private String workUnit;

    @Schema(description = "联系人职务", example = "产品经理")
    @Size(max = 128, message = "联系人职务长度不能超过 128 个字符")
    @DiffLogField(name = "联系人职务")
    private String postName;

    @Schema(description = "联系人地址", example = "杭州市西湖区")
    @Size(max = 255, message = "联系人地址长度不能超过 255 个字符")
    @DiffLogField(name = "联系人地址")
    private String address;

    @Schema(description = "排序", example = "1")
    @DiffLogField(name = "排序")
    private Integer sort;

}
