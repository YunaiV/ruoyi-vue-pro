package cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 设备台账 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_machinery")
@KeySequence("mes_dv_machinery_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvMachineryDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 设备编码
     */
    private String code;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 品牌
     */
    private String brand;
    /**
     * 规格型号
     */
    private String specification;
    /**
     * 设备类型编号
     *
     * 关联 {@link MesDvMachineryTypeDO#getId()}
     */
    private Long machineryTypeId;
    /**
     * 所属车间编号
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkshopDO#getId()}
     */
    private Long workshopId;
    /**
     * 设备状态
     *
     * 字典 {@link DictTypeConstants#MES_DV_MACHINERY_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.dv.MesDvMachineryStatusEnum}
     */
    private Integer status;
    /**
     * 最近保养时间
     */
    private LocalDateTime lastMaintenTime;
    /**
     * 最近点检时间
     */
    private LocalDateTime lastCheckTime;
    /**
     * 备注
     */
    private String remark;

}
