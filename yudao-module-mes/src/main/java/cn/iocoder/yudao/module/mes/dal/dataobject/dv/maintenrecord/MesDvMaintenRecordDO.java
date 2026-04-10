package cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvMaintenRecordStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 设备保养记录 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_mainten_record")
@KeySequence("mes_dv_mainten_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvMaintenRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 计划编号
     *
     * 关联 {@link MesDvCheckPlanDO#getId()}
     */
    private Long planId;
    /**
     * 设备编号
     *
     * 关联 {@link MesDvMachineryDO#getId()}
     */
    private Long machineryId;
    /**
     * 保养时间
     */
    private LocalDateTime maintenTime;
    /**
     * 用户编号
     *
     * 关联 AdminUserDO#getId()
     */
    private Long userId;
    /**
     * 状态
     *
     * 枚举 {@link MesDvMaintenRecordStatusEnum}
     * 字典类型 mes_mainten_record_status
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
