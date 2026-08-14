package cn.iocoder.yudao.module.hrm.controller.admin.portal.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工端员工档案 Response VO")
@Data
public class HrmPortalEmployeeRespVO {

    @Schema(description = "员工档案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工姓名", example = "张三")
    private String name;

    @Schema(description = "员工头像")
    private String avatar;

    @Schema(description = "工号", example = "HRM001")
    private String jobNumber;

    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "国家或地区", example = "中国")
    private String country;

    @Schema(description = "民族", example = "汉族")
    private String nation;

    @Schema(description = "证件类型", example = "1")
    private Integer idType;

    @Schema(description = "证件号码", example = "310101199001011234")
    private String idNumber;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "邮箱", example = "hrm@example.com")
    private String email;

    @Schema(description = "籍贯", example = "浙江杭州")
    private String nativePlace;

    @Schema(description = "出生日期")
    private LocalDateTime birthday;

    @Schema(description = "年龄", example = "28")
    private Integer age;

    @Schema(description = "户籍地址", example = "杭州市西湖区")
    private String address;

    @Schema(description = "最高学历", example = "8")
    private Integer highestEducation;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "直属上级员工编号", example = "1")
    private Long leaderEmployeeId;

    @Schema(description = "直属上级员工姓名", example = "李四")
    private String leaderEmployeeName;

    @Schema(description = "入职状态", example = "1")
    private Integer entryStatus;

    @Schema(description = "员工状态", example = "1")
    private Integer status;

    @Schema(description = "聘用形式", example = "1")
    private Integer type;

    @Schema(description = "入职时间")
    private LocalDateTime entryTime;

    @Schema(description = "入职天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long entryDay;

    @Schema(description = "试用期，单位月；0 表示无试用期", example = "3")
    private Integer probation;

    @Schema(description = "转正时间")
    private LocalDateTime regularTime;

    @Schema(description = "离职时间")
    private LocalDateTime leaveTime;

    @Schema(description = "职位名称", example = "Java 工程师")
    private String postName;

    @Schema(description = "岗位职级", example = "P6")
    private String postLevel;

    @Schema(description = "工作城市", example = "上海")
    private String workCity;

    @Schema(description = "工作地点", example = "浦东新区")
    private String workAddress;

    @Schema(description = "工作详细地址", example = "张江高科 1 号楼")
    private String workDetailAddress;

    @Schema(description = "司龄开始时间")
    private LocalDateTime companyAgeStartTime;

    @Schema(description = "司龄，单位年", example = "2")
    private Integer companyAge;

}
