package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 交易售后分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradeAfterSalePageReqVO extends PageParam {

    @ApiModelProperty(value = "售后流水号", example = "202211190847450020500077", notes = "模糊匹配")
    private String no;

    @ApiModelProperty(value = "售后状态", example = "2", notes = "参见 TradeAfterSaleStatusEnum 枚举")
    @InEnum(value = TradeAfterSaleStatusEnum.class, message = "售后状态必须是 {value}")
    private Integer status;

    @ApiModelProperty(value = "售后类型", example = "2", notes = "参见 TradeAfterSaleTypeEnum 枚举")
    @InEnum(value = TradeAfterSaleTypeEnum.class, message = "售后类型必须是 {value}")
    private Integer type;

    @ApiModelProperty(value = "订单编号", example = "18078", notes = "模糊匹配")
    private String orderNo;

    @ApiModelProperty(value = "商品 SPU 名称", example = "李四", notes = "模糊匹配")
    private String spuName;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
