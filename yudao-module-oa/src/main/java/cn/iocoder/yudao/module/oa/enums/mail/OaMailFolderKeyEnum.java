package cn.iocoder.yudao.module.oa.enums.mail;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 企业邮箱标准目录入口枚举
 *
 * 未读邮件为虚拟入口，自定义目录使用数据库编号。
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaMailFolderKeyEnum implements ArrayValuable<String> {

    INBOX("INBOX"),
    UNREAD("UNREAD"),
    SENT("SENT"),
    DRAFTS("DRAFTS"),
    TRASH("TRASH");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(OaMailFolderKeyEnum::getKey).toArray(String[]::new);

    /**
     * 入口标识
     */
    private final String key;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
