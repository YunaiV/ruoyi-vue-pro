package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
@Builder
public class NotifyDataDTO {

    private String origData;
    //1:xml 2:form
    private int format;

    //form 格式的 data;
    private Map<String,String> params;
}
