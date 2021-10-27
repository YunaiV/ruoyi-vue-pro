package cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.notify;

import cn.iocoder.yudao.coreservice.modules.pay.enums.notify.PayNotifyStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商户支付、退款等的通知 Log
 * 每次通知时，都会在该表中，记录一次 Log，方便排查问题
 *
 * @author 芋道源码
 */
@TableName("pay_notify_log")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PayNotifyLogDO extends BaseDO {

    /**
     * 日志编号，自增
     */
    private Long id;
    /**
     * 通知编号
     *
     * 关联 {@link PayNotifyTaskDO#getId()}
     */
    private Long notifyId;
    /**
     * 当前通知次数
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
