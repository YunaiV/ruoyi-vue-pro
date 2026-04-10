package cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmMiscReceiptStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmMiscReceiptTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 杂项入库单 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_misc_receipt")
@KeySequence("mes_wm_misc_receipt_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmMiscReceiptDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 入库单编码
     */
    private String code;
    /**
     * 入库单名称
     */
    private String name;
    /**
     * 杂项类型
     *
     * 枚举 {@link MesWmMiscReceiptTypeEnum}
     */
    private Integer type;
    /**
     * 来源单据 ID
     */
    private Long sourceDocId;
    /**
     * 来源单据编码
     */
    private String sourceDocCode;
    /**
     * 来源单据类型
     */
    private String sourceDocType;
    /**
     * 入库日期
     */
    private LocalDateTime receiptDate;
    /**
     * 状态
     *
     * 枚举 {@link MesWmMiscReceiptStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
