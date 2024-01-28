package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 销售订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpSaleOrderPageReqVO extends PageParam {

    @Schema(description = "销售单编号", example = "XS001")
    private String no;

    @Schema(description = "客户编号", example = "1724")
    private Long customerId;

    @Schema(description = "下单时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderTime;

    @Schema(description = "备注", example = "你猜")
    private String description;

    @Schema(description = "销售状态", example = "2")
    private Integer status;

    @Schema(description = "创建者")
    private String creator;

}