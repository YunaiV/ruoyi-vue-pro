package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusTabEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeSurveyTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTodoTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - HRM 员工档案列表 Request VO")
@Data
public class HrmEmployeeListReqVO {

    @Schema(description = "员工编号列表", example = "1,2")
    private List<Long> ids;

    @Schema(hidden = true)
    private List<Long> excludeIds;

    @Schema(description = "员工姓名或工号，模糊匹配", example = "张三")
    private String search;

    @Schema(description = "员工姓名，模糊匹配", example = "张")
    private String name;

    @Schema(description = "工号，模糊匹配", example = "HRM")
    private String jobNumber;

    @Schema(description = "手机号，模糊匹配", example = "156")
    private String mobile;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门编号数组", example = "100,101")
    private List<Long> deptIds;

    @Schema(description = "直属上级员工编号", example = "1")
    private Long leaderEmployeeId;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "入职时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] entryTime;

    @Schema(description = "职位名称，模糊匹配", example = "Java")
    private String postName;

    @Schema(description = "转正时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] regularTime;

    @Schema(description = "工作地点，模糊匹配", example = "西湖区")
    private String workAddress;

    @Schema(description = "招聘渠道编号", example = "10")
    private Long channelId;

    @Schema(description = "聘用形式", example = "1")
    @InEnum(value = HrmEmployeeTypeEnum.class, message = "聘用形式必须是 {value}")
    private Integer type;

    @Schema(description = "入职状态", example = "1")
    @InEnum(value = HrmEmployeeEntryStatusEnum.class, message = "入职状态必须是 {value}")
    private Integer entryStatus;

    @Schema(description = "员工状态", example = "1")
    @InEnum(value = HrmEmployeeStatusEnum.class, message = "员工状态必须是 {value}")
    private Integer status;

    @Schema(description = "员工状态分类", example = "12")
    @InEnum(value = HrmEmployeeStatusTabEnum.class, message = "员工状态分类必须是 {value}")
    private Integer statusCategory;

    @Schema(description = "首页人事概况筛选类型", example = "1")
    @InEnum(value = HrmEmployeeSurveyTypeEnum.class, message = "首页人事概况筛选类型必须是 {value}")
    private Integer surveyType;

    @Schema(description = "首页待办筛选类型", example = "5")
    @InEnum(value = HrmEmployeeTodoTypeEnum.class, message = "首页待办筛选类型必须是 {value}")
    private Integer todoType;

    @Schema(hidden = true)
    private LocalDateTime[] activeTime;

}
