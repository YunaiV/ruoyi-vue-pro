package cn.iocoder.yudao.module.crm.enums.business;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 商机的结束状态枚举
 *
 * @author lzxhqs
 */
@RequiredArgsConstructor
@Getter
public enum CrmBusinessEndStatusEnum implements ArrayValuable<Integer> {

    WIN(1, "赢单"),
    LOSE(2, "输单"),
    INVALID(3, "无效");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CrmBusinessEndStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 场景类型
     */
    private final Integer status;
    /**
     * 场景名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static CrmBusinessEndStatusEnum fromStatus(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }

}
