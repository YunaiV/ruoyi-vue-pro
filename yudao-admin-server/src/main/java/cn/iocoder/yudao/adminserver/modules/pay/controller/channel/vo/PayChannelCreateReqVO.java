package cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("支付渠道 创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelCreateReqVO extends PayChannelBaseVO {

    // TODO @aquan：我在想，要不这个创建和修改特殊一点。前端传递 string 过来，后端解析成对应的。因为有 code，所以我们都知道是哪个配置类。
    //  然后，在 PayChannelEnum 里，枚举每个渠道对应的配置类。另外，我们就不单独给配置类搞 vo 了。参数校验，通过手动调用 Validator 去校验。
    // 通过这样的方式，VO 和 api 都收成，一个 update，一个 create

}
