package cn.iocoder.yudao.module.mes.dal.dataobject.cal.team;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 班组 DO
 *
 * @author 芋道源码
 */
@TableName("mes_cal_team")
@KeySequence("mes_cal_team_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCalTeamDO extends BaseDO {

    /**
     * 班组编号
     */
    @TableId
    private Long id;
    /**
     * 班组编码
     */
    private String code;
    /**
     * 班组名称
     */
    private String name;
    /**
     * 班组类型
     */
    private Integer calendarType;
    /**
     * 备注
     */
    private String remark;

}
