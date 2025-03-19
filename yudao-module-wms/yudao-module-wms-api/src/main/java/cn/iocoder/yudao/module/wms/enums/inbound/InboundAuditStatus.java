package cn.iocoder.yudao.module.wms.enums.inbound;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * 入库单审批状态
 **/
@RequiredArgsConstructor
@Getter
public enum InboundAuditStatus implements ArrayValuable<Integer>, DictEnum {

    DRAFT(0, "起草中"),
    AUDIT(1, "待审批"),
    REJECT(2, "已驳回"),
    PASS(3, "已通过"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(InboundAuditStatus::getValue).toArray(Integer[]::new);

    public static String getType() {
        return InboundAuditStatus.class.getSimpleName();
    }

    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static InboundAuditStatus parse(Integer value) {
        for (InboundAuditStatus e : InboundAuditStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static InboundAuditStatus parse(String nameOrLabel) {
        for (InboundAuditStatus e : InboundAuditStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (InboundAuditStatus e : InboundAuditStatus.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    public boolean matchAny(InboundAuditStatus... status) {
        for (InboundAuditStatus s : status) {
            if(s==this) {
                return true;
            }
        }
        return false;
    }

    public boolean matchAny(Integer... values) {
        for (Integer v : values) {
            if(Objects.equals(v, this.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer[] array() {
        return VALUES;
    }

    public static enum Event {
        SUBMIT,AGREE, REJECT;
    }
}
