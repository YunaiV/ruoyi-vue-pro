package cn.iocoder.yudao.module.srm.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//srm_outbound_status 字典
@RequiredArgsConstructor
@Getter
public enum SrmOutboundStatus implements ArrayValuable<Integer> {
    NONE_OUTBOUND(0, "未出库"),
    PARTIALLY_OUTBOUND(1, "部分出库"),
    ALL_OUTBOUND(2, "全部出库");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SrmOutboundStatus::getCode).toArray();
    private static final Map<Integer, SrmOutboundStatus> STATUS_MAP = new HashMap<>();

    static {
        for (SrmOutboundStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    private final Integer code;
    private final String desc;

    public static SrmOutboundStatus fromCode(int code) {
        SrmOutboundStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的出库状态码: " + code);
        }
        return status;
    }

    public static String getDescriptionByCode(Integer code) {
        SrmOutboundStatus status = STATUS_MAP.get(code);
        return status != null ? status.getDesc() : null;
    }

    @Override
    public Integer[] array() {
        return Arrays.stream(ARRAYS).boxed().toArray(Integer[]::new);
    }
}
