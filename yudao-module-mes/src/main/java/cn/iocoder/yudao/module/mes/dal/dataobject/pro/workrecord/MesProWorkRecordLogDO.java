package cn.iocoder.yudao.module.mes.dal.dataobject.pro.workrecord;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.enums.pro.MesProWorkRecordTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 上下工记录流水 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_work_record_log")
@KeySequence("mes_pro_work_record_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProWorkRecordLogDO extends BaseDO {

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
     * 操作类型
     *
     * 字典 {@link DictTypeConstants#MES_PRO_WORK_RECORD_TYPE}
     * 枚举 {@link MesProWorkRecordTypeEnum}
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;

}
