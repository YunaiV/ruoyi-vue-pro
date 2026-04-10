package cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 外协发料单明细 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_outsource_issue_detail")
@KeySequence("mes_wm_outsource_issue_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmOutsourceIssueDetailDO extends BaseDO {

    /**
     * 明细ID
     */
    @TableId
    private Long id;
    /**
     * 行ID
     *
     * 关联 {@link MesWmOutsourceIssueLineDO#getId()}
     */
    private Long lineId;
    /**
     * 发料单ID
     *
     * 关联 {@link MesWmOutsourceIssueDO#getId()}
     */
    private Long issueId;
    /**
     * 库存ID
     *
     * 关联 {@link MesWmMaterialStockDO#getId()}
     */
    private Long materialStockId;
    /**
     * 物料ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 数量
     */
    private BigDecimal quantity;
    /**
     * 批次ID
     */
    private Long batchId;
    /**
     * 仓库ID
     *
     * 关联 {@link MesWmWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 库位ID
     */
    private Long locationId;
    /**
     * 库区ID
     */
    private Long areaId;
    /**
     * 备注
     */
    private String remark;

}
