package cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 甘特图数据项 VO
 */
@Schema(description = "管理后台 - 甘特图数据项 Response VO")
@Data
@Accessors(chain = true)
public class GanttDataRespVO {

    @Schema(description = "节点 ID（MesBizTypeConstants + '_' + 原始 ID）", example = "301_1")
    private String id;

    @Schema(description = "原始业务 ID（工单编号或任务编号，用于编辑回写）")
    private Long originalId;

    @Schema(description = "节点类型")
    private Integer type; // 使用 MesBizTypeConstants 区分，如 301=工单, 303=任务

    @Schema(description = "显示文本")
    private String text;

    @Schema(description = "父节点 ID")
    private String parent;

    @Schema(description = "工作站名称")
    private String workstation;

    @Schema(description = "工序名称")
    private String process;

    @Schema(description = "产品名称")
    private String product;

    @Schema(description = "排产数量")
    private BigDecimal quantity;

    @Schema(description = "完成进度（0~1）")
    private Float progress;

    @Schema(description = "甘特图颜色")
    private String color;

    @Schema(description = "开始时间")
    private LocalDateTime startDate;

    @Schema(description = "结束时间")
    private LocalDateTime endDate;

    @Schema(description = "生产时长")
    private Long duration;

}
