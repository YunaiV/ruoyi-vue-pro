package cn.iocoder.yudao.module.yaya.controller.app.pay.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaAppMemberOrderRespVO {

    private Long orderId;
    private Long payOrderId;
    private String status;

}
