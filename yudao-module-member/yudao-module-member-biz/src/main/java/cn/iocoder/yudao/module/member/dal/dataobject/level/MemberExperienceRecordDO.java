package cn.iocoder.yudao.module.member.dal.dataobject.level;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员经验记录 DO
 *
 * @author owen
 */
@TableName("member_experience_record")
@KeySequence("member_experience_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberExperienceRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 {@link MemberUserDO#getId()} 字段
     */
    private Long userId;
    /**
     * 业务类型
     * <p>
     * 枚举 {@link MemberExperienceBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 业务编号
     */
    private String bizId;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 经验
     */
    private Integer experience;
    /**
     * 变更后的经验
     */
    private Integer totalExperience;

}
