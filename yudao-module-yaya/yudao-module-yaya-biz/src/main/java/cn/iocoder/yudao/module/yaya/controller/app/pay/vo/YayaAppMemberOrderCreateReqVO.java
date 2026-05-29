package cn.iocoder.yudao.module.yaya.controller.app.pay.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaAppMemberOrderCreateReqVO {

    @NotBlank(message = "套餐 key 不能为空")
    private String planKey;
    private String channelCode;

}
