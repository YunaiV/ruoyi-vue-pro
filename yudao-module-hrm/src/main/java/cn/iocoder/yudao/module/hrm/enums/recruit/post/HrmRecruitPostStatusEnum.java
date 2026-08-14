package cn.iocoder.yudao.module.hrm.enums.recruit.post;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 招聘职位状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmRecruitPostStatusEnum implements ArrayValuable<Integer> {

    RECRUITING(1, "招聘中"),
    STOPPED(0, "停止招聘");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmRecruitPostStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmRecruitPostStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
