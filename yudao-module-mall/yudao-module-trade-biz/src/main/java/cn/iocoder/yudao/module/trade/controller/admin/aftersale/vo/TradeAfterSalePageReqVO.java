package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleWayEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 交易售后分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradeAfterSalePageReqVO extends PageParam {

    @Schema(description = "售后流水号", example = "202211190847450020500077")
    private String no;

    @Schema(description = "售后状态", example = "10")
    @InEnum(value = TradeAfterSaleStatusEnum.class, message = "售后状态必须是 {value}")
    private Integer status;

    @Schema(description = "售后类型", example = "20")
    @InEnum(value = TradeAfterSaleTypeEnum.class, message = "售后类型必须是 {value}")
    private Integer type;

    @Schema(description = "售后方式", example = "10")
    @InEnum(value = TradeAfterSaleWayEnum.class, message = "售后方式必须是 {value}")
    private Integer way;

    @Schema(description = "订单编号", example = "18078")
    private String orderNo;

    @Schema(description = "商品 SPU 名称", example = "李四")
    private String spuName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
