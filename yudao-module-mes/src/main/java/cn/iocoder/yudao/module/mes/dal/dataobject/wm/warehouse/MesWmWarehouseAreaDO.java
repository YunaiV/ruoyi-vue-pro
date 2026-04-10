package cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 库位 DO
 */
@TableName("mes_wm_warehouse_area")
@KeySequence("mes_wm_warehouse_area_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmWarehouseAreaDO extends BaseDO {

    /**
     * 虚拟线边库位编码（配合 {@link MesWmWarehouseDO#WIP_VIRTUAL_WAREHOUSE} 使用）
     */
    public static final String WIP_VIRTUAL_AREA = "WIP_VIRTUAL_AREA";

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 库位编码
     */
    private String code;
    /**
     * 库位名称
     */
    private String name;
    /**
     * 库区编号
     *
     * 关联 {@link MesWmWarehouseLocationDO#getId()}
     */
    private Long locationId;
    /**
     * 面积
     */
    private BigDecimal area;
    /**
     * 最大载重
     */
    private BigDecimal maxLoad;
    /**
     * 位置 X
     */
    private Integer positionX;
    /**
     * 位置 Y
     */
    private Integer positionY;
    /**
     * 位置 Z
     */
    private Integer positionZ;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    /**
     * 是否冻结
     */
    private Boolean frozen;
    /**
     * 是否允许物料混放
     */
    private Boolean allowItemMixing;
    /**
     * 是否允许批次混放
     */
    private Boolean allowBatchMixing;
    /**
     * 备注
     */
    private String remark;

}
