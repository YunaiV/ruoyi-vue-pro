package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 自提门店 Excel 导出 Request VO，参数和 DeliveryPickUpStorePageReqVO 是一致的")
@Data
public class DeliveryPickUpStoreExportReqVO {

    @Schema(description = "门店名称", example = "李四")
    private String name;

    @Schema(description = "门店手机")
    private String phone;

    @Schema(description = "区域id", example = "18733")
    private Integer areaId;

    @Schema(description = "门店状态", example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
