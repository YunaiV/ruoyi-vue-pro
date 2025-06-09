package cn.iocoder.yudao.module.fms.api.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wdy
 */
@RequiredArgsConstructor
@Getter
public enum FmsAuditStatus implements ArrayValuable<Integer> {

    // 1. 草稿阶段
    DRAFT(1, "草稿"),

    // 2. 审核流程
    SUBMITTED(2, "已提交"),
    PENDING_REVIEW(3, "未审核"),
    UNDER_REVIEW(4, "审核中"),
    APPROVED(5, "已审核"),
    REJECTED(6, "审核不通过"),
    REVOKED(7, "审核撤销"),
    ;


    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(FmsAuditStatus::getCode).toArray();
    private static final Map<Integer, FmsAuditStatus> STATUS_MAP = new HashMap<>();

    static {
        for (FmsAuditStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    /**
     * 状态
     */
    private final Integer code;
    /**
     * 状态名
     */
    private final String desc;

    /**
     * 根据状态码获取状态枚举的描述。
     *
     * @param code 状态码
     * @return 状态描述，如果没有找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        FmsAuditStatus statusEnum = STATUS_MAP.get(code);
        return statusEnum != null ? statusEnum.getDesc() : null;
    }

    public static FmsAuditStatus fromCode(int code) {
        for (FmsAuditStatus status : FmsAuditStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订购状态码: " + code);
    }

    /**
     * @return 数组
     */
    @Override
    public Integer[] array() {
        int[] arrays = ARRAYS;
        Integer[] result = new Integer[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            result[i] = arrays[i];
        }
        return result;
    }
}
