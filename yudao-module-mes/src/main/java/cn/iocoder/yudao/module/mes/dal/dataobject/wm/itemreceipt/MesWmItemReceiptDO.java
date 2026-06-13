package cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmItemReceiptStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 采购入库单 DO
 */
@TableName("mes_wm_item_receipt")
@KeySequence("mes_wm_item_receipt_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmItemReceiptDO extends BaseDO {

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
     * 来料检验单编号
     *
     * 关联 {@link MesQcIqcDO#getId()}
     */
    private Long iqcId;
    /**
     * 到货通知单编号
     *
     * 关联 {@link MesWmArrivalNoticeDO#getId()}
     */
    private Long noticeId;
    /**
     * 采购订单号
     *
     * 冗余自 {@link MesWmArrivalNoticeDO#getPurchaseOrderCode()}，也可以手动填写
     */
    private String purchaseOrderCode;
    /**
     * 供应商编号
     *
     * 关联 {@link MesMdVendorDO#getId()}
     */
    private Long vendorId;
    /**
     * 入库日期
     */
    private LocalDateTime receiptDate;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_ITEM_RECEIPT_STATUS}
     * 枚举 {@link MesWmItemReceiptStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
