package cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnSalesStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 销售退货单 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_return_sales")
@KeySequence("mes_wm_return_sales_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmReturnSalesDO extends BaseDO {

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
     * 销售订单编号
     */
    private String salesOrderCode;
    /**
     * 客户 ID
     *
     * 关联 {@link MesMdClientDO#getId()}
     */
    private Long clientId;
    /**
     * 退货日期
     */
    private LocalDateTime returnDate;
    /**
     * 退货原因
     */
    private String returnReason;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_RETURN_SALES_STATUS}
     * 枚举 {@link MesWmReturnSalesStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
