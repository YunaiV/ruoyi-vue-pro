package cn.iocoder.yudao.module.mes.enums.pro;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 生产报工状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesProFeedbackStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesProFeedbackService#createFeedback 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 审批中
     *
     * 对应 MesProFeedbackService#submitFeedback 方法
     */
    APPROVING(MesOrderStatusConstants.APPROVING, "审批中"),
    /**
     * 待检验（质检工序特有）
     *
     * 对应 MesProFeedbackService#submitFeedback 方法
     */
    UNCHECK(MesOrderStatusConstants.APPROVED, "待检验"),
    /**
     * 已完成
     *
     * 对应 MesProFeedbackService#approveFeedback 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesProFeedbackStatusEnum::getStatus).toArray(Integer[]::new);

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
