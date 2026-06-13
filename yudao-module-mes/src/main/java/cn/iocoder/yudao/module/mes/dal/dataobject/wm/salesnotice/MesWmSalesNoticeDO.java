package cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 发货通知单 DO
 */
@TableName("mes_wm_sales_notice")
@KeySequence("mes_wm_sales_notice_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmSalesNoticeDO extends BaseDO {

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
     * 销售订单编号
     */
    private String salesOrderCode;
    /**
     * 客户编号
     *
     * 关联 {@link MesMdClientDO#getId()}
     */
    private Long clientId;
    /**
     * 发货日期
     */
    private LocalDateTime salesDate;
    /**
     * 收货人
     */
    private String recipientName;
    /**
     * 联系方式
     */
    private String recipientTelephone;
    /**
     * 收货地址
     */
    private String recipientAddress;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_SALES_NOTICE_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmSalesNoticeStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
