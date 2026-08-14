package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工绩效考核流程记录 Response VO")
@Data
public class HrmPerformanceProcessRecordRespVO {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "绩效流程记录状态")
    private Integer status;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作时间")
    private LocalDateTime operateTime;

    @Schema(description = "附件地址列表")
    private List<String> fileUrls;

}
