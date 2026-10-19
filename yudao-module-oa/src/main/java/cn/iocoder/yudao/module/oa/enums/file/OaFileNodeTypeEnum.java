package cn.iocoder.yudao.module.oa.enums.file;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 云盘节点类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaFileNodeTypeEnum implements ArrayValuable<Integer> {

    FOLDER(0, "文件夹"),
    FILE(1, "文件");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaFileNodeTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 云盘节点类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    /**
     * 获得节点类型枚举
     *
     * @param type 节点类型
     * @return 节点类型枚举，不存在时返回 null
     */
    public static OaFileNodeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
