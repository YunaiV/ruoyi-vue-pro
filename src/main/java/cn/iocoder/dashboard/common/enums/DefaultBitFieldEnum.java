package cn.iocoder.dashboard.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum DefaultBitFieldEnum {

    NO(0, "否"),
    YES(1, "是");

    /**
     * 状态值
     */
    private final Integer val;
    /**
     * 状态名
     */
    private final String name;

}
