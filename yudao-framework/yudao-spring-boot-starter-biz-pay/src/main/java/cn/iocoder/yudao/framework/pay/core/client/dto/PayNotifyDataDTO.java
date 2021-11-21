package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;


/**
 * 支付订单回调，渠道的统一通知请求数据
 */
@Data
@ToString
@Builder
public class PayNotifyDataDTO {

    //返回的所有参数
    private String body;


    //form 格式的 data;
    private Map<String,String> params;
}
