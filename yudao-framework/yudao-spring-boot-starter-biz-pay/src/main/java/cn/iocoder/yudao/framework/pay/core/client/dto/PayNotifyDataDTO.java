package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;


@Data
@ToString
@Builder
public class PayNotifyDataDTO {

    //返回的所有参数
    private String body;


    //form 格式的 data;
    private Map<String,String> params;
}
