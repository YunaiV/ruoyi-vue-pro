package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧生成状态的枚举：角色形象、场景图、视觉资产、镜头视频、成片合成共用；为空表示未生成
 *
 * 镜头视频与 AI 视频一致：10 待处理 → 20 生成中 → 30 转存中 → 40 已完成 / 50 已失败；
 * 图片生成、成片合成只使用 20 生成中 → 40 已完成 / 50 已失败
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaStatusEnum implements ArrayValuable<Integer> {

    PENDING(10, "待处理"),
    IN_PROGRESS(20, "生成中"),
    SAVING(30, "转存中"),
    SUCCESS(40, "已完成"),
    FAIL(50, "已失败");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
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
     * 是否为未结束的状态（待处理、生成中、转存中）
     *
     * @param status 状态
     * @return 是否未结束
     */
    public static boolean isRunning(Integer status) {
        return ObjectUtils.equalsAny(status, PENDING.status, IN_PROGRESS.status, SAVING.status);
    }

}
