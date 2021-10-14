package cn.iocoder.yudao.adminserver.modules.pay.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

/**
 * 商户支付、退款等的通知
 * 在支付系统收到支付渠道的支付、退款的结果后，需要不断的通知到业务系统，直到成功。
 * TODO 芋艿：待完善
 *
 * @author 芋道源码
 */
@Data
public class PayNotifyDO extends BaseDO {
}
