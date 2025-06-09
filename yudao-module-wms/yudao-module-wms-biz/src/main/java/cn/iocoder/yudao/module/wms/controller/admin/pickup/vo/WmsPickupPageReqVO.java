package cn.iocoder.yudao.module.wms.controller.admin.pickup.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,create_time,upstream_id,upstream_code,warehouse_id,upstream_type
 */
@Schema(description = "管理后台 - 拣货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsPickupPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "22380")
    private Long warehouseId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "来源单据ID", example = "")
    private Long upstreamId;

    @Schema(description = "来源单据编码", example = "")
    private String upstreamCode;

    @Schema(description = "来源单据类型", example = "")
    private Integer upstreamType;
}
