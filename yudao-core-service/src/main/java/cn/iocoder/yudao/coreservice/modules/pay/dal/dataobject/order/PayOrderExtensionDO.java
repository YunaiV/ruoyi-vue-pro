package cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 交易扩展表
 */
@TableName("pay_transaction_extension")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PayOrderExtensionDO extends BaseDO {

    /**
     * 编号，自增
     */
    private Integer id;
    /**
     * 交易编号 {@link PayTransactionDO#getId()}
     */
    private Integer transactionId;
    /**
     * 选择的支付渠道
     */
    private Integer payChannel;
    /**
     * 生成传输给第三方的订单号
     *
     * 唯一索引
     */
    private String transactionCode;
    /**
     * 扩展内容
     *
     * 异步通知的时候填充回调的数据
     */
    private String extensionData;
    /**
     * 发起交易的 IP
     */
    private String createIp;
    /**
     * 状态
     *
     * @see cn.iocoder.mall.payservice.enums.transaction.PayTransactionStatusEnum
     * 注意，只包含上述枚举的 WAITING 和 SUCCESS
     */
    private Integer status;

}
