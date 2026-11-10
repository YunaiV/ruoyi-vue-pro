package cn.iocoder.yudao.module.ai.enums.music;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 音乐状态的枚举
 *
 * 状态链：10 待处理 → 20 生成中 → 30 转存中 → 40 已完成；任一步失败或超时进入 50 已失败
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiMusicStatusEnum implements ArrayValuable<Integer> {

    /**
     * 已插入占位行，还未拿到中转 task_id（提交请求内）
     */
    PENDING(10, "待处理"),
    /**
     * 已提交中转，task_id 非空、clip_id 为空，等待同步任务查询
     */
    IN_PROGRESS(20, "生成中"),
    /**
     * 中转已成功并拆分为每首一行，封面、音频暂存中转源地址，等待转存到文件服务
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

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiMusicStatusEnum::getStatus).toArray(Integer[]::new);

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
