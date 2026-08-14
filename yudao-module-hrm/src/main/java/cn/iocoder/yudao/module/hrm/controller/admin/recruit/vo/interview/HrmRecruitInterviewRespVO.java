package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 招聘面试 Response VO")
@Data
public class HrmRecruitInterviewRespVO {

    @Schema(description = "面试编号", example = "1024")
    private Long id;

    @Schema(description = "候选人编号", example = "1024")
    private Long candidateId;

    @Schema(description = "面试方式", example = "1")
    private Integer type;

    @Schema(description = "面试轮次", example = "1")
    private Integer stageNumber;

    @Schema(description = "主面试官员工编号", example = "1")
    private Long interviewEmployeeId;

    @Schema(description = "主面试官姓名", example = "张三")
    private String interviewEmployeeName;

    @Schema(description = "其他面试官员工编号数组", example = "[2, 3]")
    private List<Long> otherInterviewEmployeeIds;

    @Schema(description = "其他面试官姓名数组", example = "[\"李四\", \"王五\"]")
    private List<String> otherInterviewEmployeeNames;

    @Schema(description = "面试时间")
    private LocalDateTime interviewTime;

    @Schema(description = "面试地址", example = "上海会议室 A")
    private String address;

    @Schema(description = "备注", example = "请带作品集")
    private String remark;

    @Schema(description = "面试结果", example = "1")
    private Integer result;

    @Schema(description = "评价")
    private String evaluate;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
