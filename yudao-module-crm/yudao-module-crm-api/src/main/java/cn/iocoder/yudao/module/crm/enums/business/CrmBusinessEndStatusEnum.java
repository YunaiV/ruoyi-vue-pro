package cn.iocoder.yudao.module.crm.enums.business;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
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
public enum CrmBusinessEndStatusEnum implements IntArrayValuable {

    WIN(1, "赢单"),
    LOSE(2, "输单"),
    INVALID(3, "无效");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmBusinessEndStatusEnum::getStatus).toArray();

    /**
     * 场景类型
     */
    private final Integer status;
    /**
     * 场景名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static CrmBusinessEndStatusEnum fromStatus(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }

}
