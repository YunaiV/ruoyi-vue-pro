package cn.iocoder.yudao.module.oa.enums.officialdoc;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 公文收文办理状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaOfficialDocHandleStatusEnum implements ArrayValuable<Integer> {

    WAIT_CLAIM(0, "待签收"),
    CLAIMED(1, "已签收"),
    PROCESSING(2, "办理中"),
    COMPLETED(3, "已办结");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaOfficialDocHandleStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 办理状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    public static OaOfficialDocHandleStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
