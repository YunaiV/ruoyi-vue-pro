package cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 杂项入库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmMiscReceiptPageReqVO extends PageParam {

    @Schema(description = "入库单编码", example = "MR2026030001")
    private String code;

    @Schema(description = "入库单名称", example = "退料入库单")
    private String name;

    @Schema(description = "杂项类型", example = "1")
    private Integer type;

    @Schema(description = "来源单据编号", example = "DOC2026030001")
    private String sourceDocCode;

    @Schema(description = "来源单据类型", example = "WORK_ORDER")
    private String sourceDocType;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "入库日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] receiptDate;

}
