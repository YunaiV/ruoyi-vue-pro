package cn.iocoder.yudao.module.wms.enums.stock;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 库存类型
 **/
@RequiredArgsConstructor
@Getter
public enum WmsMoveExecuteStatus implements ArrayValuable<Integer>, DictEnum {

    DRAFT(0, "草稿"),
    MOVED(1, "已执行"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsMoveExecuteStatus::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsMoveExecuteStatus parse(Integer value) {
        for (WmsMoveExecuteStatus e : WmsMoveExecuteStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsMoveExecuteStatus parse(String nameOrLabel) {
        for (WmsMoveExecuteStatus e : WmsMoveExecuteStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsMoveExecuteStatus e : WmsMoveExecuteStatus.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public Integer[] array() {
        return VALUES;
    }
}
