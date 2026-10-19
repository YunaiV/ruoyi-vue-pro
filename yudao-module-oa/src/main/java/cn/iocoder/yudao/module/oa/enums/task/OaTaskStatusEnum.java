package cn.iocoder.yudao.module.oa.enums.task;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 任务状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaTaskStatusEnum implements ArrayValuable<Integer> {

    NEW(1, "新任务", 20),
    RECEIVED(2, "已接收", 40),
    IN_PROGRESS(3, "进行中", 60),
    SUBMITTED(4, "已提交", 80),
    COMPLETED(5, "已完成", 100);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaTaskStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;
    /**
     * 完成进度
     */
    private final Integer progress;

    public static OaTaskStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
