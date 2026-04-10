package cn.iocoder.yudao.module.mes.enums.pro;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 生产任务状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesProTaskStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesProTaskService#createTask 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 已完成
     *
     * 对应 MesProTaskService#finishTaskByOrderId 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成"),
    /**
     * 已取消
     *
     * 对应 MesProTaskService#cancelTaskByOrderId 方法
     */
    CANCELED(MesOrderStatusConstants.CANCELLED, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesProTaskStatusEnum::getStatus).toArray(Integer[]::new);

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

    /**
     * 判断是否为终态（已完成 或 已取消）
     *
     * @param status 状态值
     * @return 是否为终态
     */
    public static boolean isEndStatus(Integer status) {
        return ObjectUtils.equalsAny(status, FINISHED.status, CANCELED.status);
    }

}

