package cn.iocoder.yudao.module.product.enums.comment;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品评论的审批状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ProductCommentAuditStatusEnum implements ArrayValuable<Integer> {

    NONE(0, "待审核"),
    APPROVE(1, "审批通过"),
    REJECT(2, "审批不通过"),;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ProductCommentAuditStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 审批状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
