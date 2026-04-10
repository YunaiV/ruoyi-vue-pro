package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 生产退料单状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmReturnIssueStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmReturnIssueService#createReturnIssue 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待检验（已确认，等待质检）
     *
     * 对应 MesWmReturnIssueService#submitReturnIssue 方法
     */
    CONFIRMED(MesOrderStatusConstants.CONFIRMED, "待检验"), // "UNCHECK"
    /**
     * 待上架（检验完成，等待仓库上架）
     *
     * 对应 MesWmReturnIssueService#submitReturnIssue 方法
     */
    APPROVING(MesOrderStatusConstants.APPROVING, "待上架"), // "UNSTOCK"
    /**
     * 待执行退料（上架完成，等待执行退料操作）
     *
     * 对应 MesWmReturnIssueService#stockReturnIssue 方法
     */
    APPROVED(MesOrderStatusConstants.APPROVED, "待执行退料"), // "UNEXECUTE"
    /**
     * 已完成（退料执行完成，库存已更新）
     *
     * 对应 MesWmReturnIssueService#finishReturnIssue 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成"),
    /**
     * 已取消
     *
     * 对应 MesWmReturnIssueService#cancelReturnIssue 方法
     */
    CANCELED(MesOrderStatusConstants.CANCELLED, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmReturnIssueStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

// DONE @AI：状态流转逻辑已在 MesWmReturnIssueServiceImpl 中实现，参考 confirmReturnIssue、submitReturnIssue、stockReturnIssue、finishReturnIssue 方法
