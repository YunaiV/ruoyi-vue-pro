package cn.iocoder.yudao.module.oa.enums.officialdoc;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 公文收文类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaOfficialDocReceiveTypeEnum implements ArrayValuable<Integer> {

    /**
     * 主送，需办理
     */
    MAIN(0, "主送"),
    /**
     * 抄送，仅知会
     */
    COPY(1, "抄送");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaOfficialDocReceiveTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 收文类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    public static OaOfficialDocReceiveTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
