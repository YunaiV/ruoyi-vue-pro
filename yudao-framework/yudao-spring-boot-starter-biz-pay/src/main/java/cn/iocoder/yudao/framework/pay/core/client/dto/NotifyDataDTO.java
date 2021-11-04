package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

// TODO @jason：NotifyDataDTO=》PayNotifyDataDTO。另外注释记得加起来哈
@Data
@ToString
@Builder
public class NotifyDataDTO {

    // TODO @jason：这个字段，改成 body
    private String origData;
    // TODO @jason：这个字段如果暂时没用，可以去掉。
    //1:xml 2:form
    private int format;

    //form 格式的 data;
    private Map<String,String> params;
}
