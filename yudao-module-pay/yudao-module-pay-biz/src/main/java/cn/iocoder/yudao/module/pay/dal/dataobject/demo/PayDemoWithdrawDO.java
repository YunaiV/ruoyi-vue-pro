package cn.iocoder.yudao.module.pay.dal.dataobject.demo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.enums.demo.PayDemoWithdrawStatusEnum;
import cn.iocoder.yudao.module.pay.enums.demo.PayDemoWithdrawTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 示例提现订单
 *
 * 演示业务系统的转账业务
 */
@TableName(value ="pay_demo_withdraw", autoResultMap = true)
@KeySequence("pay_demo_withdraw_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PayDemoWithdrawDO extends BaseDO {

    /**
     * 提现单编号，自增
     */
    @TableId
    private Long id;

    /**
     * 提现标题
     */
    private String subject;
    /**
     * 提现金额，单位：分
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
     * 提现方式
     *
     * 枚举 {@link PayDemoWithdrawTypeEnum}
     */
    private Integer type;
    /**
     * 提现状态
     *
     * 枚举 {@link PayDemoWithdrawStatusEnum}
     */
    private Integer status;

    // ========== 转账相关字段 ==========

    /**
     * 转账单编号
     *
     * 关联 {@link PayTransferDO#getId()}
     */
    private Long payTransferId;
    /**
     * 转账渠道
     *
     * 枚举 {@link cn.iocoder.yudao.module.pay.enums.PayChannelEnum}
     */
    private String transferChannelCode;
    /**
     * 转账成功时间
     */
    private LocalDateTime transferTime;
    /**
     * 转账错误提示
     */
    private String transferErrorMsg;

}