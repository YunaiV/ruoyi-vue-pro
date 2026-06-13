package cn.iocoder.yudao.module.mes.dal.dataobject.pro.workrecord;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 用户工作站绑定关系（当前快照） DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_work_record")
@KeySequence("mes_pro_work_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProWorkRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 AdminUserDO#getId()
     */
    private Long userId;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 当前状态
     *
     * 字典 {@link DictTypeConstants#MES_PRO_WORK_RECORD_TYPE}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.pro.MesProWorkRecordTypeEnum}
     */
    private Integer type;
    /**
     * 上工时间
     */
    private LocalDateTime clockInTime;
    /**
     * 下工时间
     */
    private LocalDateTime clockOutTime;
    /**
     * 备注
     */
    private String remark;

}
