package cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 仓库 DO
 */
@TableName("mes_wm_warehouse")
@KeySequence("mes_wm_warehouse_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmWarehouseDO extends BaseDO {

    /**
     * 虚拟线边库编码
     *
     * 业务背景：在线边库模式下，物料从总仓库发放到车间，但未被工序消耗前，这部分物料属于在制品（WIP）。
     * 为了在系统中实现“发料与实际消耗”的解耦，并在不增加物理库位管理复杂度的前提下防止生产报工被负库存阻塞，
     * 系统默认提供一个虚拟的线边库统管这些在制品。
     */
    public static final String WIP_VIRTUAL_WAREHOUSE = "WIP_VIRTUAL_WAREHOUSE";

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 仓库编码
     */
    private String code;
    /**
     * 仓库名称
     */
    private String name;
    /**
     * 仓库地址
     */
    private String address;
    /**
     * 面积
     */
    private BigDecimal area;
    /**
     * 负责人用户编号
     *
     * 关联 system_users 表的 id 字段
     */
    private Long chargeUserId;
    /**
     * 是否冻结
     */
    private Boolean frozen;
    /**
     * 备注
     */
    private String remark;

}
