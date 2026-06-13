package cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmOutsourceReceiptStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 外协入库单 DO
 */
@TableName("mes_wm_outsource_receipt")
@KeySequence("mes_wm_outsource_receipt_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmOutsourceReceiptDO extends BaseDO {

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
     * 外协工单编号
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
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
     * 字典 {@link DictTypeConstants#MES_WM_OUTSOURCE_RECEIPT_STATUS}
     * 枚举 {@link MesWmOutsourceReceiptStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
