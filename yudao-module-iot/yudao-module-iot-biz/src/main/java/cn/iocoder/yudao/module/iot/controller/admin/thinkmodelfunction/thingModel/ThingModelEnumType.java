package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThingModelEnumType extends ThingModelDataType {
    private Map<String, String> specs; // 枚举值和描述的键值对
}
