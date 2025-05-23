package cn.iocoder.yudao.module.iot.protocol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 消息方向枚举
 *
 * @author haohao
 */
@Getter
@AllArgsConstructor
public enum IotMessageDirectionEnum {

    /**
     * 上行消息（设备到平台）
     */
    UPSTREAM("upstream", "上行"),

    /**
     * 下行消息（平台到设备）
     */
    DOWNSTREAM("downstream", "下行");

    /**
     * 方向编码
     */
    private final String code;

    /**
     * 方向名称
     */
    private final String name;

    /**
     * 根据编码获取消息方向
     *
     * @param code 方向编码
     * @return 消息方向枚举，如果未找到返回 null
     */
    public static IotMessageDirectionEnum getByCode(String code) {
        for (IotMessageDirectionEnum direction : values()) {
            if (direction.getCode().equals(code)) {
                return direction;
            }
        }
        return null;
    }
}