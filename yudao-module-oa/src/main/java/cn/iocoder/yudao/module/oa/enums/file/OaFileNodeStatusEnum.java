package cn.iocoder.yudao.module.oa.enums.file;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 云盘节点状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaFileNodeStatusEnum implements ArrayValuable<Integer> {

    NORMAL(0, "正常"),
    RECYCLED(1, "回收站");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaFileNodeStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 云盘节点状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    /**
     * 获得节点状态枚举
     *
     * @param status 节点状态
     * @return 节点状态枚举，不存在时返回 null
     */
    public static OaFileNodeStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
