package cn.iocoder.yudao.module.wms.enums.outbound;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 出库单审批状态
 **/
@RequiredArgsConstructor
@Getter
public enum WmsOutboundAuditStatus implements ArrayValuable<Integer>, DictEnum {

    DRAFT(0, "起草中"),
    AUDITING(1, "待审批"),
    REJECT(2, "已驳回"),
    PASS(3, "已通过"),
    FINISHED(4, "已出库"),
    /**
     * 5:已作废
     **/
    ABANDONED(5, "作废"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsOutboundAuditStatus::getValue).toArray(Integer[]::new);

    public static String getType() {
        return WmsOutboundAuditStatus.class.getSimpleName();
    }

    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举
     **/
    public static WmsOutboundAuditStatus parse(Integer value) {
        for (WmsOutboundAuditStatus e : WmsOutboundAuditStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 value 匹配枚举
     **/
    public static List<WmsOutboundAuditStatus> parse(Collection<Integer> value) {
        return value.stream().map(WmsOutboundAuditStatus::parse).toList();
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsOutboundAuditStatus parse(String nameOrLabel) {
        for (WmsOutboundAuditStatus e : WmsOutboundAuditStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsOutboundAuditStatus e : WmsOutboundAuditStatus.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    public boolean matchAny(WmsOutboundAuditStatus... status) {
        for (WmsOutboundAuditStatus s : status) {
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

        SUBMIT("提交审核"),ABANDON("废弃出库单"),AGREE("通过审核"), REJECT("拒绝审核"),FINISH("完成出库");

        @Getter
        private String label;

        Event(String label) {
            this.label = label;
        }

    }
}
