package cn.iocoder.yudao.module.ai.enums.video;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 视频状态的枚举
 *
 * 状态链：10 待处理 → 20 生成中 → 30 转存中 → 40 已完成；任一步失败或超时进入 50 已失败。
 * 服务商的任务状态（{@link cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskStatus}）只在同步时映射，不直接写入
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiVideoStatusEnum implements ArrayValuable<Integer> {

    /**
     * 已插入记录，还未拿到服务商的任务编号（提交请求内）
     */
    PENDING(10, "待处理"),
    /**
     * 已提交服务商，task_id 非空，等待同步任务查询
     */
    IN_PROGRESS(20, "生成中"),
    /**
     * 服务商已生成成功，等待转存到文件服务；转存时按 task_id 重新查询，获得最新的临时地址
     */
    SAVING(30, "转存中"),
    SUCCESS(40, "已完成"),
    FAIL(50, "已失败");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiVideoStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 是否为未结束的状态（待处理、生成中、转存中）
     *
     * @param status 状态
     * @return 是否未结束
     */
    public static boolean isRunning(Integer status) {
        return ObjectUtils.equalsAny(status, PENDING.status, IN_PROGRESS.status, SAVING.status);
    }

}
