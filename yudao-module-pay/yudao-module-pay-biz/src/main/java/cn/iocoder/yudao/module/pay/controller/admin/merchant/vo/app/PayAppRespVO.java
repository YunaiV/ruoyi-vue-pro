package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;

import lombok.*;

import java.time.LocalDateTime;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 支付应用信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppRespVO extends PayAppBaseVO {

    @ApiModelProperty(value = "应用编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
