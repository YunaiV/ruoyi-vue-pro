package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 招聘候选人 Response VO")
@Data
@Accessors(chain = true)
public class HrmRecruitCandidateRespVO {

    @Schema(description = "候选人编号", example = "1024")
    private Long id;

    @Schema(description = "候选人姓名", example = "张三")
    private String name;

    @Schema(description = "手机号码", example = "15601691399")
    private String mobile;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "年龄", example = "28")
    private Integer age;

    @Schema(description = "邮箱", example = "candidate@example.com")
    private String email;

    @Schema(description = "应聘职位编号", example = "1024")
    private Long postId;

    @Schema(description = "应聘职位名称", example = "Java 开发工程师")
    private String postName;

    @Schema(description = "应聘职位状态", example = "1")
    private Integer postStatus;

    @Schema(description = "用人部门编号", example = "100")
    private Long deptId;

    @Schema(description = "用人部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "招聘负责人员工编号", example = "1")
    private Long ownerEmployeeId;

    @Schema(description = "招聘负责人姓名", example = "李四")
    private String ownerEmployeeName;

    @Schema(description = "面试轮次", example = "1")
    private Integer stageNumber;

    @Schema(description = "工作年限", example = "5")
    private Integer workTime;

    @Schema(description = "学历", example = "5")
    private Integer education;

    @Schema(description = "毕业院校", example = "浙江大学")
    private String graduateSchool;

    @Schema(description = "最近工作单位", example = "某科技公司")
    private String latestWorkPlace;

    @Schema(description = "招聘渠道编号", example = "1")
    private Long channelId;

    @Schema(description = "招聘渠道名称", example = "BOSS 直聘")
    private String channelName;

    @Schema(description = "备注", example = "沟通意向强")
    private String remark;

    @Schema(description = "候选人状态", example = "1")
    private Integer status;

    @Schema(description = "淘汰原因", example = "技能不匹配")
    private String eliminate;

    @Schema(description = "状态更新时间")
    private LocalDateTime statusUpdateTime;

    @Schema(description = "入职时间")
    private LocalDateTime entryTime;

    @Schema(description = "简历附件地址数组", example = "[\"https://example.com/resume.pdf\"]")
    private List<String> resumeUrls;

    @Schema(description = "当前面试编号", example = "1024")
    private Long interviewId;

    @Schema(description = "面试方式", example = "3")
    private Integer interviewType;

    @Schema(description = "主面试官员工编号", example = "1")
    private Long interviewEmployeeId;

    @Schema(description = "主面试官姓名", example = "王五")
    private String interviewEmployeeName;

    @Schema(description = "其他面试官员工编号数组", example = "[2, 3]")
    private List<Long> otherInterviewEmployeeIds;

    @Schema(description = "其他面试官姓名数组", example = "[\"赵六\", \"钱七\"]")
    private List<String> otherInterviewEmployeeNames;

    @Schema(description = "面试时间")
    private LocalDateTime interviewTime;

    @Schema(description = "面试地址", example = "上海市浦东新区")
    private String interviewAddress;

    @Schema(description = "面试结果", example = "1")
    private Integer interviewResult;

    @Schema(description = "转入的员工编号", example = "100")
    private Long employeeId;

    @Schema(description = "创建人", example = "1")
    private String creator;

    @Schema(description = "创建人名称", example = "管理员")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
