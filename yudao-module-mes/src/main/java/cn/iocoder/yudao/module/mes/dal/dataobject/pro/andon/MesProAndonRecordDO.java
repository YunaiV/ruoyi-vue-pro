package cn.iocoder.yudao.module.mes.dal.dataobject.pro.andon;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.enums.pro.MesProAndonLevelEnum;
import cn.iocoder.yudao.module.mes.enums.pro.MesProAndonStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 安灯呼叫记录 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_andon_record")
@KeySequence("mes_pro_andon_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProAndonRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 安灯配置编号
     *
     * 关联 {@link MesProAndonConfigDO#getId()}
     */
    private Long configId;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 发起用户编号
     *
     * 关联 system_users#id
     */
    private Long userId;
    /**
     * 生产工单编号
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 呼叫原因（快照值，不随配置变更）
     */
    private String reason;
    /**
     * 级别（快照值）
     *
     * 枚举 {@link MesProAndonLevelEnum}
     */
    private Integer level;
    /**
     * 处置状态
     *
     * 枚举 {@link MesProAndonStatusEnum}
     */
    private Integer status;
    /**
     * 处置时间
     */
    private LocalDateTime handleTime;
    /**
     * 处置人编号
     *
     * 关联 system_users#id
     */
    private Long handlerUserId;
    /**
     * 备注
     */
    private String remark;

}
