package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

// TODO @jason：注释要写下哈。字段不要使用 // 注释，非标准
@Data
@ToString
@Builder
public class PayNotifyDataDTO {

    //返回的所有参数
    private String body;


    //form 格式的 data;
    private Map<String,String> params;
}
