package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MesWmMiscIssueTypeEnum implements ArrayValuable<Integer> {

    ADJUST(1, "库存调整"),
    SCRAP(2, "报废出库");

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return Arrays.stream(values()).map(MesWmMiscIssueTypeEnum::getType).toArray(Integer[]::new);
    }

}
