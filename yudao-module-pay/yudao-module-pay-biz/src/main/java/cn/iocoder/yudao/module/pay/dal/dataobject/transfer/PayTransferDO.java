package cn.iocoder.yudao.module.pay.dal.dataobject.transfer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferStatusRespEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 转账单 DO
 *
 * @author jason
 */
@TableName(value ="pay_transfer", autoResultMap = true)
@KeySequence("pay_transfer_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PayTransferDO extends BaseDO {

    /**
     * 编号
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
     * 转账渠道编号
     *
     * 关联 {@link PayChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 转账渠道编码
     *
     * 枚举 {@link PayChannelEnum}
     */
    private String channelCode;
    /**
     * 类型
     *
     * 枚举 {@link PayTransferTypeEnum}
     */
    private Integer type;

    // ========== 商户相关字段 ==========

    /**
     * 商户订单编号
     *
     * 例如说，内部系统 A 的订单号，需要保证每个 PayAppDO 唯一
     */
    private String merchantOrderId;

    /**
     * 转账标题
     */
    private String subject;

    // ========== 转账相关字段 ==========
    /**
     * 转账金额，单位：分
     */
    private Integer price;
    /**
     * 转账状态
     *
     * 枚举 {@link PayTransferStatusRespEnum}
     */
    private Integer status;
    /**
     * 订单转账成功时间
     */
    private LocalDateTime successTime;
    /**
     * 转账成功的转账拓展单编号
     *
     * 关联 {@link PayTransferExtensionDO#getId()}
     */
    private Long extensionId;
    /**
     * 转账成功的转账拓展单号
     *
     * 关联 {@link PayTransferExtensionDO#getNo()}
     */
    private String no;
    /**
     * 收款人信息，不同类型和渠道不同
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> payeeInfo;

}