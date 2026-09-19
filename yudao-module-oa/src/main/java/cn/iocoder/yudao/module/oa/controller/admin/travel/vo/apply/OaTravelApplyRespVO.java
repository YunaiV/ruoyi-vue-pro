package cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply;

import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelApplyDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 出差申请 Response VO")
@Data
public class OaTravelApplyRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "单据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CC202609140001")
    private String no;

    @Schema(description = "出差事由", example = "赴北京参加客户项目验收")
    private String reason;

    @Schema(description = "开始日期", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime startTime;

    @Schema(description = "结束日期", type = "integer", format = "int64", example = "1789380000000")
    private LocalDateTime endTime;

    @Schema(description = "同行人", example = "张三、李四")
    private String companion;

    @Schema(description = "预计费用", example = "100.00")
    private BigDecimal estimatedPrice;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "出差天数", example = "1")
    private Integer days;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "-1")
    private Integer status;

    @Schema(description = "报销状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean reimburseStatus;

    @Schema(description = "流程实例编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "申请人编号", example = "1024")
    private String creator;

    @Schema(description = "申请人姓名", example = "张三")
    private String creatorName;

    @Schema(description = "申请部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "申请部门", example = "行政部")
    private String deptName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

    @Schema(description = "明细")
    private List<OaTravelApplyDO.Item> items;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;


}
