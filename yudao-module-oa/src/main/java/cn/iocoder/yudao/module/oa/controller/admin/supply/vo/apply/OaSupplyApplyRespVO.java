package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply;

import cn.iocoder.yudao.module.oa.enums.supply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.OaSupplyIssueRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用品领用申请 Response VO")
@Data
public class OaSupplyApplyRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "领用日期", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime applyTime;

    @Schema(description = "使用类型", example = "1")
    private Integer useType;

    @Schema(description = "领取方式", example = "1")
    private Integer pickupMethod;

    @Schema(description = "申请事由", example = "领用部门日常办公用品")
    private String reason;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "申请单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "YC202609140001")
    private String no;

    @Schema(description = "申请人编号", example = "1024")
    private String creator;

    @Schema(description = "申请人", example = "张三")
    private String creatorName;

    @Schema(description = "申请部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deptId;

    @Schema(description = "申请部门", example = "行政部")
    private String deptName;

    @Schema(description = "单据状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "-1")
    private Integer status;

    @Schema(description = "流程实例编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

    @Schema(description = "领用明细")
    private List<OaSupplyIssueRespVO> items;

}
