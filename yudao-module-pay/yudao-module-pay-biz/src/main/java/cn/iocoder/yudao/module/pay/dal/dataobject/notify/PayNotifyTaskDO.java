package cn.iocoder.yudao.module.pay.dal.dataobject.notify;

import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 商户支付、退款等的通知
 * 在支付系统收到支付渠道的支付、退款的结果后，需要不断的通知到业务系统，直到成功。
 *
 * @author 芋道源码
 */
@TableName("pay_notify_task")
@KeySequence("pay_notify_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PayNotifyTaskDO extends BaseDO {

    /**
     * 通知频率，单位为秒。
     *
     * 算上首次的通知，实际是一共 1 + 8 = 9 次。
     */
    public static final Integer[] NOTIFY_FREQUENCY = new Integer[]{
            15, 15, 30, 180,
            1800, 1800, 1800, 3600
    };

    /**
     * 编号，自增
     */
    private Long id;
    /**
     * 商户编号
     *
     * 关联 {@link PayMerchantDO#getId()}
     */
    private Long merchantId;
    /**
     * 应用编号
     *
     * 关联 {@link PayAppDO#getId()}
     */
    private Long appId;
    /**
     * 通知类型
     *
     * 外键 {@link PayNotifyTypeEnum}
     */
    private Integer type;
    /**
     * 数据编号，根据不同 type 进行关联：
     *
     * 1. {@link PayNotifyTypeEnum#ORDER} 时，关联 {@link PayOrderDO#getId()}
     * 2. {@link PayNotifyTypeEnum#REFUND} 时，关联 {@link PayRefundDO#getId()}
     */
    private Long dataId;
    /**
     * 商户订单编号
     */
    private String merchantOrderId;
    /**
     * 通知状态
     *
     * 外键 {@link PayNotifyStatusEnum}
     */
    private Integer status;
    /**
     * 下一次通知时间
     */
    private LocalDateTime nextNotifyTime;
    /**
     * 最后一次执行时间
     */
    private LocalDateTime lastExecuteTime;
    /**
     * 当前通知次数
     */
    private Integer notifyTimes;
    /**
     * 最大可通知次数
     */
    private Integer maxNotifyTimes;
    /**
     * 通知地址
     */
    private String notifyUrl;

}
