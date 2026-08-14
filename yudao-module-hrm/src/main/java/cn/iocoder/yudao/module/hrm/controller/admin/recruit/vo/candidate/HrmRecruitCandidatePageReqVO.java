package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - HRM 招聘候选人分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmRecruitCandidatePageReqVO extends PageParam {

    @Schema(description = "姓名、手机号码或邮箱，模糊匹配", example = "张三")
    private String search;

    @Schema(description = "职位编号", example = "1024")
    private Long postId;

    @Schema(description = "招聘负责人员工编号", example = "1")
    private Long ownerEmployeeId;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "最小年龄", example = "20")
    private Integer minAge;

    @Schema(description = "最大年龄", example = "35")
    private Integer maxAge;

    @Schema(description = "最小工作年限", example = "1")
    private Integer minWorkTime;

    @Schema(description = "最大工作年限", example = "5")
    private Integer maxWorkTime;

    @Schema(description = "学历", example = "5")
    private Integer education;

    @Schema(description = "毕业院校，模糊匹配", example = "浙江大学")
    private String graduateSchool;

    @Schema(description = "最近工作单位，模糊匹配", example = "某科技公司")
    private String latestWorkPlace;

    @Schema(description = "渠道编号", example = "1")
    private Long channelId;

    @Schema(description = "面试官员工编号", example = "1")
    private Long interviewEmployeeId;

    @Schema(description = "面试时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] interviewTime;

    @Schema(description = "创建人用户编号", example = "1")
    private Long creator;

    @Schema(description = "候选人状态", example = "1")
    @InEnum(value = HrmRecruitCandidateStatusEnum.class, message = "候选人状态必须是 {value}")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
