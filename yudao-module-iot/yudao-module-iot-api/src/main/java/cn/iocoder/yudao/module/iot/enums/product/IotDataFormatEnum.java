package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 产品数据格式枚举类
 *
 * @author ahh
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/message-parsing">阿里云 - 什么是消息解析</a>
 */
@AllArgsConstructor
@Getter
public enum IotDataFormatEnum implements ArrayValuable<Integer> {

    JSON(0, "标准数据格式（JSON）"),
    CUSTOMIZE(1, "透传/自定义");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotDataFormatEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
