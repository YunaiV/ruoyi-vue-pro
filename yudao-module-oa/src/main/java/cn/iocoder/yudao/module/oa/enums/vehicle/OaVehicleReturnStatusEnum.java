package cn.iocoder.yudao.module.oa.enums.vehicle;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 车辆归还状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaVehicleReturnStatusEnum implements ArrayValuable<Integer> {

    NOT_EFFECTIVE(0, "未生效"),
    PENDING_RETURN(1, "待还车"),
    RETURNING(2, "还车中"),
    RETURNED(3, "已还车");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaVehicleReturnStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 车辆归还状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    public static OaVehicleReturnStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
