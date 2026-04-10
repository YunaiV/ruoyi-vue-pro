package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - MES 销售退货相关
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 销售退货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmReturnSalesPageReqVO extends PageParam {

    @Schema(description = "退货单编号", example = "RS202603010001")
    private String code;

    @Schema(description = "退货单名称", example = "销售退货")
    private String name;

    @Schema(description = "销售订单编号", example = "SO202603010001")
    private String salesOrderCode;

    @Schema(description = "客户 ID", example = "1")
    private Long clientId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "退货日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] returnDate;

}
