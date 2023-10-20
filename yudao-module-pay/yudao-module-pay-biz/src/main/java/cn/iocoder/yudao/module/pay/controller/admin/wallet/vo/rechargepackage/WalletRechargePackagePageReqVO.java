package cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.rechargepackage;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 充值套餐分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WalletRechargePackagePageReqVO extends PageParam {

    @Schema(description = "套餐名", example = "李四")
    private String name;

    // TODO @jason：payPrice 和 bonusPrice 可以去掉。。。一般太少检索啦；

    @Schema(description = "支付金额", example = "16454")
    private Integer payPrice;

    @Schema(description = "赠送金额", example = "20887")
    private Integer bonusPrice;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
