package cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("支付应用信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppRespVO extends PayAppBaseVO {

    @ApiModelProperty(value = "应用编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
