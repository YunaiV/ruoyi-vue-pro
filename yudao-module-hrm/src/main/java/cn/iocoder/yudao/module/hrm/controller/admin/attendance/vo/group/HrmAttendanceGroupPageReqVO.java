package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - HRM 考勤组分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmAttendanceGroupPageReqVO extends PageParam {

    @Schema(description = "考勤组名称", example = "默认考勤组")
    private String name;

    @Schema(description = "是否默认考勤组", example = "true")
    private Boolean defaultStatus;

}
