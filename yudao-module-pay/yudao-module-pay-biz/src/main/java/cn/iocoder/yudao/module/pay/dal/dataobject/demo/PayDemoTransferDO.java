package cn.iocoder.yudao.module.pay.dal.dataobject.demo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// TODO 芋艿：需要详细 review
/**
 * 示例转账订单
 *
 * 演示业务系统的转账业务
 */
@TableName(value ="pay_demo_transfer", autoResultMap = true)
@KeySequence("pay_demo_transfer_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PayDemoTransferDO extends BaseDO {

    /**
     * 订单编号
     */
    @TableId
    private Long id;

    /**
     * 应用编号
     *
     * 关联 {@link PayAppDO#getId()}
     */
    private Long appId;

    /**
     * 转账类型
     * <p>
     * 枚举 {@link PayTransferTypeEnum}
     */
    private Integer type;

    /**
     * 转账金额，单位：分
     */
    private Integer price;

    /**
     * 收款人姓名
     */
    private String userName;

    /**
     * 支付宝登录号
     */
    private String alipayLogonId;

    /**
     * 微信 openId
     */
    private String openid;

    /**
     * 转账状态
     */
    private Integer transferStatus;

    /**
     * 转账单编号
     */
    private Long payTransferId;

    /**
     * 转账支付成功渠道
     */
    private String payChannelCode;

    /**
     * 转账支付时间
     */
    private LocalDateTime transferTime;

}