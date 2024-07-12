package cn.iocoder.yudao.module.ai.enums.write;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 写作类型的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiWriteTypeEnum implements IntArrayValuable {

    WRITING(1, "撰写", "请撰写一篇关于 [{}] 的文章。文章的内容格式：{}，语气：{}，语言：{}，长度：{}。请确保涵盖主要内容，不需要除了正文内容外的其他回复，如标题、额外的解释或道歉。"),
    REPLY(2, "回复", "请针对如下内容：[{}] 做个回复。回复内容参考：[{}], 回复格式：{}，语气：{}，语言：{}，长度：{}。不需要除了正文内容外的其他回复，如标题、开头、额外的解释或道歉。");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    /**
     * 模版
     */
    private final String prompt;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiWriteTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
