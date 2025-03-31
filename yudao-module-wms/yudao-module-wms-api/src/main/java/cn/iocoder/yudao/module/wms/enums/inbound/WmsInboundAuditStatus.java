package cn.iocoder.yudao.module.wms.enums.inbound;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 入库单审批状态
 **/
@RequiredArgsConstructor
@Getter
public enum WmsInboundAuditStatus implements ArrayValuable<Integer>, DictEnum {

    DRAFT(0, "起草中"),
    AUDITING(1, "待审批"),
    REJECT(2, "已驳回"),
    PASS(3, "已通过"),
    FORCE_FINISHED(4, "强制完成"),
    ABANDONED(5, "已作废"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsInboundAuditStatus::getValue).toArray(Integer[]::new);

    public static String getType() {
        return WmsInboundAuditStatus.class.getSimpleName();
    }

    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举
     **/
    public static WmsInboundAuditStatus parse(Integer value) {
        for (WmsInboundAuditStatus e : WmsInboundAuditStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 value 匹配枚举
     **/
    public static List<WmsInboundAuditStatus> parse(Collection<Integer> value) {
        return value.stream().map(WmsInboundAuditStatus::parse).toList();
    }



    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsInboundAuditStatus parse(String nameOrLabel) {
        for (WmsInboundAuditStatus e : WmsInboundAuditStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsInboundAuditStatus e : WmsInboundAuditStatus.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    public boolean matchAny(WmsInboundAuditStatus... status) {
        for (WmsInboundAuditStatus s : status) {
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

    @Getter
    public enum Event {

        SUBMIT("提交审核"),AGREE("通过审核"), REJECT("拒绝审核"),ABANDON("作废"),FORCE_FINISH("强制完成");

        private String label;

        Event(String label) {
            this.label = label;
        }

    }
}
