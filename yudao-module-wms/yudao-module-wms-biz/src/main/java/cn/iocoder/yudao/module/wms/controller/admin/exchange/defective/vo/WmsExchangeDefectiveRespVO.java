package cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,exchange_id,creator,update_time,create_time,product_id,qty,remark,id,from_bin_id,to_bin_id,updater
 */
@Schema(description = "管理后台 - 良次换货详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsExchangeDefectiveRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "换货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7120")
    @ExcelProperty("换货单ID")
    private Long exchangeId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6112")
    @ExcelProperty("标准产品ID")
    private Long productId;

    @Schema(description = "源仓位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6726")
    @ExcelProperty("源仓位ID")
    private Long fromBinId;

    @Schema(description = "目的仓位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32001")
    @ExcelProperty("目的仓位ID")
    private Long toBinId;

    @Schema(description = "换货量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("换货量")
    private Integer qty;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "产品", example = "")
    @ExcelProperty("产品")
    private WmsProductRespSimpleVO product;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;
}
