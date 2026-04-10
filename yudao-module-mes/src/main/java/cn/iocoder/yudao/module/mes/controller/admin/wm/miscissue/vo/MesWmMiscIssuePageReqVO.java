package cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 杂项出库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmMiscIssuePageReqVO extends PageParam {

    @Schema(description = "出库单编号", example = "MI20260302001")
    private String code;

    @Schema(description = "出库单名称", example = "库存调整出库")
    private String name;

    @Schema(description = "杂项类型", example = "1")
    private Integer type;

    @Schema(description = "来源单据编号", example = "DOC20260302001")
    private String sourceDocCode;

    @Schema(description = "来源单据类型", example = "PURCHASE_ORDER")
    private String sourceDocType;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "出库日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] issueDate;

}
