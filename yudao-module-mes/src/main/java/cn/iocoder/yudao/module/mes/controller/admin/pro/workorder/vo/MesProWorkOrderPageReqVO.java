package cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 生产工单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProWorkOrderPageReqVO extends PageParam {

    @Schema(description = "工单编码", example = "WO-001")
    private String code;

    @Schema(description = "工单名称", example = "生产工单-A")
    private String name;

    @Schema(description = "工单类型", example = "1")
    private Integer type;

    @Schema(description = "来源单据编号", example = "SO-001")
    private String orderSourceCode;

    @Schema(description = "产品编号", example = "100")
    private Long productId;

    @Schema(description = "客户编号", example = "300")
    private Long clientId;

    @Schema(description = "需求日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] requestDate;

}
