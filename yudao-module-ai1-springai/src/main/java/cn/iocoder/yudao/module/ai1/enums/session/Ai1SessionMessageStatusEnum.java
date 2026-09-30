package cn.iocoder.yudao.module.ai1.enums.session;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI1 会话消息生成状态的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum Ai1SessionMessageStatusEnum implements ArrayValuable<Integer> {

    GENERATING(0, "生成中"), // 助手消息占位，可按消息编号续传
    SUCCESS(1, "完成"),
    FAILED(2, "失败");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(Ai1SessionMessageStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isGenerating(Integer status) {
        return ObjUtil.equal(GENERATING.status, status);
    }

}
