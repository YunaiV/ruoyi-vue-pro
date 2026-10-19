package cn.iocoder.yudao.module.oa.enums.seal;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用印业务状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaSealUseStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待处理"),
    COMPLETED(1, "已完成"),
    BORROWED(2, "外借中"),
    RETURNED(3, "已归还"),
    OVERDUE(4, "已逾期");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaSealUseStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 用印业务状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    public static OaSealUseStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
