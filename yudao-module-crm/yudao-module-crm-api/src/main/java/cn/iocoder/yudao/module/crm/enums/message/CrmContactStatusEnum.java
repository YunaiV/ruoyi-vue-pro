package cn.iocoder.yudao.module.crm.enums.message;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * CRM 联系状态
 *
 * @author dhb52
 */
@RequiredArgsConstructor
@Getter
public enum CrmContactStatusEnum implements IntArrayValuable {

    NEEDED_TODAY(1, "今日需联系"),
    EXPIRED(2, "已逾期"),
    ALREADY_CONTACT(3, "已联系"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmContactStatusEnum::getType).toArray();

    /**
     * 状态
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
