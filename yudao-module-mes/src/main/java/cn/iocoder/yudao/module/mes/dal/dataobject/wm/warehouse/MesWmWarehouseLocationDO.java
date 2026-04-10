package cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 库区 DO
 */
@TableName("mes_wm_warehouse_location")
@KeySequence("mes_wm_warehouse_location_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmWarehouseLocationDO extends BaseDO {

    /**
     * 虚拟线边库区编码（配合 {@link MesWmWarehouseDO#WIP_VIRTUAL_WAREHOUSE} 使用）
     */
    public static final String WIP_VIRTUAL_LOCATION = "WIP_VIRTUAL_LOCATION";

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 库区编码
     */
    private String code;
    /**
     * 库区名称
     */
    private String name;
    /**
     * 仓库编号
     *
     * 关联 {@link MesWmWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 面积
     */
    private BigDecimal area;
    /**
     * 库位管理状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer areaStatus;
    /**
     * 是否冻结
     */
    private Boolean frozen;
    /**
     * 备注
     */
    private String remark;

}
