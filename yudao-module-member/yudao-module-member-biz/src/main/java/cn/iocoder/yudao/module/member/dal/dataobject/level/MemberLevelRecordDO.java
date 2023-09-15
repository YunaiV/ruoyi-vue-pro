package cn.iocoder.yudao.module.member.dal.dataobject.level;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员等级记录 DO
 *
 * 用户每次等级发生变更时，记录一条日志
 *
 * @author owen
 */
@TableName("member_level_record")
@KeySequence("member_level_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevelRecordDO extends BaseDO {

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
     * 等级编号
     *
     * 关联 {@link MemberLevelDO#getId()} 字段
     */
    private Long levelId;
    /**
     * 会员等级
     *
     * 冗余 {@link MemberLevelDO#getLevel()} 字段
     */
    private Integer level;
    /**
     * 享受折扣
     */
    private Integer discountPercent;
    /**
     * 升级经验
     */
    private Integer experience;
    /**
     * 会员此时的经验
     */
    private Integer userExperience;
    /**
     * 备注
     */
    private String remark;
    /**
     * 描述
     */
    private String description;

}
