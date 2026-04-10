package cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 维修工单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesDvRepairPageReqVO extends PageParam {

    @Schema(description = "维修工单编码", example = "REP2024001")
    private String code;

    @Schema(description = "维修工单名称", example = "注塑机维修")
    private String name;

    @Schema(description = "设备编号", example = "1")
    private Long machineryId;

    @Schema(description = "维修结果", example = "1")
    private Integer result;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "报修日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] requireDate;

}
