package cn.iocoder.yudao.module.jl.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import cn.iocoder.yudao.framework.common.core.StringArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 支付订单的状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SalesLeadStatusEnums implements StringArrayValuable {

    PotentialConsultation("0", "潜在咨询"),
    KeyFocus("1", "重点关注"),
    PendingDeal("2", "待成交"),
    CompletedTransaction("3", "已成交"),
    EmergencyProject("4", "临时应急项目"),
    LostDeal("5", "丢单"),
    ;

    private final String status;
    private final String name;

    @Override
    public List<String> array() {
        return new ArrayList<>();
    }

}