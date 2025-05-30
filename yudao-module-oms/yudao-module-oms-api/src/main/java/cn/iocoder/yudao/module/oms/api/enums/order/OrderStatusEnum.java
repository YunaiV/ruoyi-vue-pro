package cn.iocoder.yudao.module.oms.api.enums.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderStatusEnum {

    DRAFT(0, "草稿"),
    PENDING_REVIEW(1, "待审核");
    private Integer type;
    private String name;
}
