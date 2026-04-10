package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 杂项出库单状态枚举
 *
 * 对应字典 mes_wm_misc_issue_status
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmMiscIssueStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmMiscIssueService#createMiscIssue 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待执行出库
     *
     * 对应 MesWmMiscIssueService#submitMiscIssue 方法
     */
    APPROVED(MesOrderStatusConstants.APPROVED, "待执行出库"),
    /**
     * 已完成
     *
     * 对应 MesWmMiscIssueService#finishMiscIssue 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成"),
    /**
     * 已取消
     *
     * 对应 MesWmMiscIssueService#cancelMiscIssue 方法
     */
    CANCELED(MesOrderStatusConstants.CANCELLED, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmMiscIssueStatusEnum::getStatus).toArray(Integer[]::new);

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
