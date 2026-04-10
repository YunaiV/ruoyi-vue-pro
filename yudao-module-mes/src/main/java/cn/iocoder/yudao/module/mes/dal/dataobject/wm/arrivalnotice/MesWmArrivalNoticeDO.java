package cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmArrivalNoticeStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 到货通知单 DO
 */
@TableName("mes_wm_arrival_notice")
@KeySequence("mes_wm_arrival_notice_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmArrivalNoticeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 通知单编码
     */
    private String code;
    /**
     * 通知单名称
     */
    private String name;
    /**
     * 采购订单编号
     */
    private String purchaseOrderCode;
    /**
     * 供应商编号
     *
     * 关联 {@link MesMdVendorDO#getId()}
     */
    private Long vendorId;
    /**
     * 到货日期
     */
    private LocalDateTime arrivalDate;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 联系电话
     */
    private String contactTelephone;
    /**
     * 状态
     *
     * 枚举 {@link MesWmArrivalNoticeStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
