package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ERP 审核状态枚举
 * TODO 芋艿：目前只有待审批、已审批两个状态，未来接入工作流后，会丰富下：待提交（草稿）=》已提交（待审核）=》审核通过、审核不通过；另外，工作流需要支持“反审核”，把工作流退回到原点；
 *
 * @author wdy
 */
@RequiredArgsConstructor
@Getter
public enum ErpAuditStatus implements ArrayValuable<Integer> {

    // 1. 草稿阶段
    DRAFT(0, "草稿"),

    // 2. 审核流程
    SUBMITTED(11, "已提交"),
    PROCESS(10, "未审核"),
    PROCESSING(12, "审核中"),
    APPROVE(20, "已审核"),
    REJECTED(14, "审核不通过"),
    REVERSED(5, "反审核");

    private static final Map<Integer, ErpAuditStatus> STATUS_MAP = new HashMap<>();

    static {
        for (ErpAuditStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ErpAuditStatus::getCode).toArray();

    /**
     * 状态
     */
    private final Integer code;
    /**
     * 状态名
     */
    private final String name;

    /**
     * 根据状态码获取状态枚举的描述。
     *
     * @param code 状态码
     * @return 状态描述，如果没有找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        ErpAuditStatus statusEnum = STATUS_MAP.get(code);
        return statusEnum != null ? statusEnum.getName() : null;
    }
    public static ErpAuditStatus fromCode(int code) {
        for (ErpAuditStatus status : ErpAuditStatus.values()) {
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
