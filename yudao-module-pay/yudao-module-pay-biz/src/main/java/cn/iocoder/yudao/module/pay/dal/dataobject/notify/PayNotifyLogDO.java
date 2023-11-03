package cn.iocoder.yudao.module.pay.dal.dataobject.notify;

import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商户支付、退款等的通知 Log
 * 每次通知时，都会在该表中，记录一次 Log，方便排查问题
 *
 * @author 芋道源码
 */
@TableName("pay_notify_log")
@KeySequence("pay_notify_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayNotifyLogDO extends BaseDO {

    /**
     * 日志编号，自增
     */
    private Long id;
    /**
     * 通知任务编号
     *
     * 关联 {@link PayNotifyTaskDO#getId()}
     */
    private Long taskId;
    /**
     * 第几次被通知
     *
     * 对应到 {@link PayNotifyTaskDO#getNotifyTimes()}
     */
    private Integer notifyTimes;
    /**
     * HTTP 响应结果
     */
    private String response;
    /**
     * 支付通知状态
     *
     * 外键 {@link PayNotifyStatusEnum}
     */
    private Integer status;

}
