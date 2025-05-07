package cn.iocoder.yudao.module.pay.dal.dataobject.demo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 转账渠道
     *
     * 枚举 {@link cn.iocoder.yudao.module.pay.enums.PayChannelEnum}
     */
    private String channelCode;

    /**
     * 转账标题
     */
    private String subject;

    /**
     * 转账金额，单位：分
     */
    private Integer price;

    /**
     * 收款人账号
     */
    private String userAccount;
    /**
     * 收款人姓名
     */
    private String userName;

    /**
     * 转账状态
     *
     * 枚举 {@link PayTransferStatusEnum}
     */
    private Integer transferStatus;

    /**
     * 转账单编号
     *
     * 关联 {@link PayTransferDO#getId()}
     */
    private Long payTransferId;
    /**
     * 转账成功时间
     */
    private LocalDateTime transferTime;

}