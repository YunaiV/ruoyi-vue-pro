package cn.iocoder.yudao.module.srm.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ERP 执行状态枚举
 *
 * @author fish
 */
@RequiredArgsConstructor
@Getter
public enum SrmExecutionStatus implements ArrayValuable<Integer> {

    PENDING(1, "待执行"),
    IN_PROGRESS(2, "执行中"),
    COMPLETED(3, "已完成"),
    PAUSED(4, "已暂停"),
    CANCELLED(5, "已取消"),
    FAILED(6, "执行失败"),
    ON_HOLD(7, "挂起中");
    /**
     * 0	待执行（TO_BE_EXECUTED）	订单刚创建，尚未入库、未开始发货
     * 1	执行中（EXECUTING）	有 部分入库记录，表示订单已开始履行
     * 2	已完成（COMPLETED）	全部入库完成，交付结束，采购完毕
     * 3	已取消（CANCELED）	采购单被作废、取消（用户操作、驳回、撤回）
     * 4	已关闭（CLOSED）	被管理员/系统手动关闭，或满足某种“异常终止”条件
     */

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SrmExecutionStatus::getCode).toArray();
    private static final Map<Integer, SrmExecutionStatus> STATUS_MAP = new HashMap<>();

    static {
        for (SrmExecutionStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 根据状态码获取状态枚举的描述
     *
     * @param code 状态码
     * @return 状态描述，如果没有找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        SrmExecutionStatus statusEnum = STATUS_MAP.get(code);
        return statusEnum != null ? statusEnum.getDesc() : null;
    }

    /**
     * 根据状态码获取状态枚举
     *
     * @param code 状态码
     * @return 状态枚举
     */
    public static SrmExecutionStatus fromCode(int code) {
        SrmExecutionStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的执行状态码: " + code);
        }
        return status;
    }

    @Override
    public Integer[] array() {
        return Arrays.stream(ARRAYS).boxed().toArray(Integer[]::new);
    }
} 