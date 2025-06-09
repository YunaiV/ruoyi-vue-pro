package cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : exchange_id,create_time,product_id,qty,remark,from_bin_id,to_bin_id
 */
@Schema(description = "管理后台 - 良次换货详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsExchangeItemPageReqVO extends PageParam {

    @Schema(description = "换货单ID", example = "7120")
    private Long exchangeId;

    @Schema(description = "标准产品ID", example = "6112")
    private Long productId;

    @Schema(description = "源仓位ID", example = "6726")
    private Long fromBinId;

    @Schema(description = "目的仓位ID", example = "32001")
    private Long toBinId;

    @Schema(description = "换货量")
    private Integer qty;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
