package cn.iocoder.yudao.module.iot.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IOT  访问方式枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotAccessModeEnum  {

    READ("r"),
    WRITE("w"),
    READ_WRITE("rw");

    private final String mode;

    public String getMode() {
        return mode;
    }

}
