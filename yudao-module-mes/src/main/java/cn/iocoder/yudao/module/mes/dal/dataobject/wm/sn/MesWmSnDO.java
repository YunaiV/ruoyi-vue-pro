package cn.iocoder.yudao.module.mes.dal.dataobject.wm.sn;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES SN 码 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_sn")
@KeySequence("mes_wm_sn_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmSnDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 批次 UUID（用于标记同一批次生成的 SN 码）
     */
    private String uuid;
    /**
     * SN 码（唯一）
     */
    private String code;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 批次号
     */
    private String batchCode;
    // TODO @芋艿：【暂时不处理】看看后续要不要去掉这个字段。
    /**
     * 生产工单编号
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;

}
