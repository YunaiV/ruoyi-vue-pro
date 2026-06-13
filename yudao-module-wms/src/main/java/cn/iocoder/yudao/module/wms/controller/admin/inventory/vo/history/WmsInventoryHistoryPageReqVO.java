package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.history;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS 库存流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInventoryHistoryPageReqVO extends PageParam {

    @Schema(description = "商品编号", example = "ITEM001")
    private String itemCode;

    @Schema(description = "商品名称", example = "红富士苹果")
    private String itemName;

    @Schema(description = "商品 SKU 编号", example = "1024")
    private Long skuId;

    @Schema(description = "规格编号", example = "SKU001")
    private String skuCode;

    @Schema(description = "规格名称", example = "10kg 箱装")
    private String skuName;

    @Schema(description = "仓库编号", example = "2048")
    private Long warehouseId;

    @Schema(description = "单据号", example = "RK202605110001")
    private String orderNo;

    @Schema(description = "单据类型", example = "1")
    private Integer orderType;

    @Schema(description = "操作时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
