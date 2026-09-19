package cn.iocoder.yudao.module.oa.enums.mail;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 邮件写信方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaMailComposeModeEnum implements ArrayValuable<String> {

    NEW("new", "新邮件"),
    DRAFT("draft", "编辑草稿"),
    REPLY("reply", "回复"),
    REPLY_ALL("replyAll", "回复全部"),
    FORWARD("forward", "转发");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(OaMailComposeModeEnum::getValue).toArray(String[]::new);

    /**
     * 值
     */
    private final String value;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
