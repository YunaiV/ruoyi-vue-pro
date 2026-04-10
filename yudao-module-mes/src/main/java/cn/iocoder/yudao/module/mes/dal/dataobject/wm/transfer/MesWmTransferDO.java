package cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransferStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransferTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 转移单 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_transfer")
@KeySequence("mes_wm_transfer_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmTransferDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 转移单编号
     */
    private String code;
    /**
     * 转移单名称
     */
    private String name;
    /**
     * 转移单类型
     *
     * 枚举 {@link MesWmTransferTypeEnum}
     */
    private Integer type;

    /**
     * 是否配送
     */
    private Boolean deliveryFlag;
    /**
     * 收货人
     */
    private String recipientName;
    /**
     * 联系方式
     */
    private String recipientTelephone;
    /**
     * 目的地
     */
    private String destinationAddress;
    /**
     * 承运商
     */
    private String carrier;
    /**
     * 运输单号
     */
    private String shippingNumber;
    /**
     * 是否已确认
     */
    private Boolean confirmFlag;

    /**
     * 转移日期
     */
    private LocalDateTime transferDate;

    /**
     * 状态
     *
     * 枚举 {@link MesWmTransferStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
