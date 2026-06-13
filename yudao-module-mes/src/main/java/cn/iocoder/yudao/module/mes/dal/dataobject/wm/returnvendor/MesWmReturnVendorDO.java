package cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnVendorStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 供应商退货单 DO
 */
@TableName("mes_wm_return_vendor")
@KeySequence("mes_wm_return_vendor_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmReturnVendorDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 退货单编号
     */
    private String code;
    /**
     * 退货单名称
     */
    private String name;
    /**
     * 采购订单编号
     */
    private String purchaseOrderCode;
    /**
     * 供应商 ID
     *
     * 关联 {@link MesMdVendorDO#getId()}
     */
    private Long vendorId;
    /**
     * 退货日期
     */
    private LocalDateTime returnDate;
    /**
     * 退货原因
     */
    private String returnReason;
    /**
     * 物流单号
     */
    private String transportCode;
    /**
     * 物流联系电话
     */
    private String transportTelephone;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_RETURN_VENDOR_STATUS}
     * 枚举 {@link MesWmReturnVendorStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
