package cn.iocoder.yudao.module.pay.dal.dataobject.transfer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
     * 类型
     */
    private Integer type;

    /**
     * 应用编号
     */
    private Long appId;

    /**
     * 商户订单编号
     */
    private String merchantOrderId;

    /**
     * 转账金额，单位：分
     */
    private Integer price;

    /**
     * 转账标题
     */
    private String title;

    /**
     * 收款人信息，不同类型和渠道不同
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> payeeInfo;

    /**
     * 转账状态
     */
    private Integer status;

    /**
     * 订单转账成功时间
     *
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
     * 转账渠道编号
     */
    private Long channelId;

    /**
     * 转账渠道编码
     */
    private String channelCode;
}