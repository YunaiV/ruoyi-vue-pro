package cn.iocoder.yudao.module.pay.dal.dataobject.transfer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Map;

// TODO @jason：转账是不是类似 refund，不用拓展单呀？支付做拓展单的原因，是因为它存在不确定性，可以切换多种；转账和退款，都是明确方式的；
/**
 * 转账拓展单 DO
 *
 * @author jason
 */
@TableName(value ="pay_transfer_extension",autoResultMap = true)
@KeySequence("pay_transfer_extension_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PayTransferExtensionDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 转账单号
     */
    private String no;

    /**
     * 转账单编号
     */
    private Long transferId;

    /**
     * 转账渠道编号
     */
    private Long channelId;

    /**
     * 转账渠道编码
     */
    private String channelCode;

    /**
     * 支付渠道的额外参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> channelExtras;

    /**
     * 转账状态
     */
    private Integer status;

    /**
     * 支付渠道异步通知的内容
     */
    private String channelNotifyData;

}