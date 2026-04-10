package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 生产退料类型枚举
 *
 * 对应字典 mes_wm_return_issue_type
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmReturnIssueTypeEnum implements ArrayValuable<Integer> {

    /**
     * 余料退料（余料退回，直接合格）
     */
    REMAINDER(1, "余料退料"),
    /**
     * 不良退料（不良品退回）
     */
    DEFECTIVE(2, "不良退料"),
    /**
     * 其他退料（其他原因退料）
     */
    OTHER(3, "其他退料");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmReturnIssueTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
