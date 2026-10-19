package cn.iocoder.yudao.module.oa.enums.file;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 云盘文件列表范围枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaFileScopeEnum implements ArrayValuable<String> {

    MY("my", "我的文件"),
    SHARED("shared", "共享文件"),
    FAVORITE("favorite", "我的收藏"),
    RECYCLE("recycle", "回收站");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(OaFileScopeEnum::getScope).toArray(String[]::new);

    /**
     * 列表范围
     */
    private final String scope;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
