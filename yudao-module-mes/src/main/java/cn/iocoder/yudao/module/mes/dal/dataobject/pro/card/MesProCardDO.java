package cn.iocoder.yudao.module.mes.dal.dataobject.pro.card;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 生产流转卡 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_card")
@KeySequence("mes_pro_card_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProCardDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 流转卡编码
     */
    private String code;
    /**
     * 生产工单编号
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 产品物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 批次号
     */
    private String batchCode;
    /**
     * 流转数量
     */
    private BigDecimal transferedQuantity;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_PRO_WORK_ORDER_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.pro.MesProWorkOrderStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
