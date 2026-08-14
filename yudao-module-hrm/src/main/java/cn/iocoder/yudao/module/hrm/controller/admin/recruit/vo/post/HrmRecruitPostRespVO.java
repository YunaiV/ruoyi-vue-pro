package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 招聘职位 Response VO")
@Data
public class HrmRecruitPostRespVO {

    @Schema(description = "招聘职位编号", example = "1024")
    private Long id;

    @Schema(description = "职位名称", example = "Java 开发工程师")
    private String postName;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "用人部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "工作性质", example = "1")
    private Integer jobNature;

    @Schema(description = "工作城市地区编号", example = "310115")
    private Integer areaId;

    @Schema(description = "工作城市地区名称", example = "上海 上海市 浦东新区")
    private String areaName;

    @Schema(description = "招聘人数", example = "3")
    private Integer recruitNum;

    @Schema(description = "招聘原因", example = "团队扩编")
    private String reason;

    @Schema(description = "工作经验", example = "3")
    private Integer workTime;

    @Schema(description = "学历要求", example = "4")
    private Integer educationRequire;

    @Schema(description = "最低薪资", example = "20000")
    private BigDecimal minSalary;

    @Schema(description = "最高薪资", example = "30000")
    private BigDecimal maxSalary;

    @Schema(description = "薪资单位", example = "2")
    private Integer salaryUnit;

    @Schema(description = "最小年龄", example = "22")
    private Integer minAge;

    @Schema(description = "最大年龄", example = "35")
    private Integer maxAge;

    @Schema(description = "最迟到岗时间")
    private LocalDateTime latestEntryTime;

    @Schema(description = "招聘负责人员工编号", example = "1")
    private Long ownerEmployeeId;

    @Schema(description = "招聘负责人姓名", example = "张三")
    private String ownerEmployeeName;

    @Schema(description = "面试官员工编号数组", example = "[1, 2]")
    private List<Long> interviewEmployeeIds;

    @Schema(description = "面试官姓名数组", example = "[\"张三\", \"李四\"]")
    private List<String> interviewEmployeeNames;

    @Schema(description = "职位描述")
    private String description;

    @Schema(description = "紧急程度", example = "1")
    private Integer emergencyLevel;

    @Schema(description = "职位类型编号", example = "100")
    private Long postTypeId;

    @Schema(description = "职位类型名称", example = "研发")
    private String postTypeName;

    @Schema(description = "职位状态", example = "1")
    private Integer status;

    @Schema(description = "停止原因", example = "岗位暂停")
    private String stopReason;

    @Schema(description = "已入职人数", example = "2")
    private Long hasEntryNum;

    @Schema(description = "招聘进度百分比", example = "40.00")
    private BigDecimal recruitSchedule;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
