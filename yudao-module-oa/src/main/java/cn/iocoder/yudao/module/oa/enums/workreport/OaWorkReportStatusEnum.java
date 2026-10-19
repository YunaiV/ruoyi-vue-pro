package cn.iocoder.yudao.module.oa.enums.workreport;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 工作汇报状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaWorkReportStatusEnum implements ArrayValuable<Integer> {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaWorkReportStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    /**
     * 获得指定状态对应的枚举
     *
     * @param status 状态
     * @return 对应枚举，未匹配时返回 null
     */
    public static OaWorkReportStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
