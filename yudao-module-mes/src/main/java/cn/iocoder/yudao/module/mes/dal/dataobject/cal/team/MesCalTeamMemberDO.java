package cn.iocoder.yudao.module.mes.dal.dataobject.cal.team;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 班组成员 DO
 *
 * @author 芋道源码
 */
@TableName("mes_cal_team_member")
@KeySequence("mes_cal_team_member_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCalTeamMemberDO extends BaseDO {

    /**
     * 班组成员编号
     */
    @TableId
    private Long id;
    /**
     * 班组编号
     *
     * 关联 {@link MesCalTeamDO#getId()}
     */
    private Long teamId;
    /**
     * 用户编号
     *
     * 关联 system_users 表
     */
    private Long userId;
    /**
     * 备注
     */
    private String remark;

}
