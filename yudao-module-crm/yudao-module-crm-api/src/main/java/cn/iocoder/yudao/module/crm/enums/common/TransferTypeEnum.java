package cn.iocoder.yudao.module.crm.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @puhui999：这个可以不用哈
/**
 * Crm 负责人转移后原负责人的处理方式
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum TransferTypeEnum implements IntArrayValuable {

    REMOVE(1, "移除"),
    TEAM(2, "转为团队成员");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TransferTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
