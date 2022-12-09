package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 支付商户信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayMerchantPageReqVO extends PageParam {

    @Schema(title = "商户号")
    private String no;

    @Schema(title = "商户全称")
    private String name;

    @Schema(title = "商户简称")
    private String shortName;

    @Schema(title = "开启状态")
    private Integer status;

    @Schema(title = "备注")
    private String remark;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "创建时间")
    private LocalDateTime[] createTime;

}
