package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
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
public enum ErpAuditStatus implements IntArrayValuable {

    CLOSED(0, "已关闭"),// 已关闭
    OPENED(1, "已开启"),// 已开启
    MANUAL_CLOSED(91, "手动关闭"),//手动关闭
    PROCESS(10, "未审核"), // 未审核
    APPROVE(20, "已审核"), // 审核通过
    ;
    private static final Map<Integer, ErpAuditStatus> STATUS_MAP = new HashMap<>();

    static {
        for (ErpAuditStatus status : values()) {
            STATUS_MAP.put(status.status, status);
        }
    }

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ErpAuditStatus::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    /**
     * 根据状态码获取状态枚举的描述。
     *
     * @param status 状态码
     * @return 状态描述，如果没有找到则返回null
     */
    public static String getDescriptionByStatus(Integer status) {
        ErpAuditStatus statusEnum = STATUS_MAP.get(status);
        return statusEnum != null ? statusEnum.getName() : null;
    }
}
