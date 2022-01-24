package cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("支付商户信息创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayMerchantCreateReqVO extends PayMerchantBaseVO {

}
