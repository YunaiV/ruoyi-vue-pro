package cn.iocoder.yudao.module.iot.messagebus.core.local;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocalIotMessage {

    private String topic;

    private Object message;

}
