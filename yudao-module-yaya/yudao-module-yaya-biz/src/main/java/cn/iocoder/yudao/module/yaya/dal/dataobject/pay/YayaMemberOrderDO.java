package cn.iocoder.yudao.module.yaya.dal.dataobject.pay;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName("yaya_member_order")
@KeySequence("yaya_member_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaMemberOrderDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long planId;
    private String planKey;
    private Long priceCent;
    private String currency;
    private String status;
    private Long payOrderId;
    private String channelCode;
    private String payChannelCode;
    private LocalDateTime paidAt;

}
