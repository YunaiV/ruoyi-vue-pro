package cn.iocoder.yudao.module.system.enums.somle;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 公司编码
 **/
@RequiredArgsConstructor
@Getter
public enum CompanyCode implements ArrayValuable<Integer>, DictEnum {

    // ========== WMS  编码段 0~99 ==========
    SOMILE(6, "宁波索迈"),
    ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(CompanyCode::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static CompanyCode parse(Integer value) {
        for (CompanyCode e : CompanyCode.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static CompanyCode parse(String nameOrLabel) {
        for (CompanyCode e : CompanyCode.values()) {
            if (e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (CompanyCode e : CompanyCode.values()) {
            if (e.getLabel().equalsIgnoreCase(nameOrLabel)) {
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
