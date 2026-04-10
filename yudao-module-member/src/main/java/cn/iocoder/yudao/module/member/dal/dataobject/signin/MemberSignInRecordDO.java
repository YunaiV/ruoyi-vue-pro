package cn.iocoder.yudao.module.member.dal.dataobject.signin;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 签到记录 DO
 *
 * @author 芋道源码
 */
@TableName("member_sign_in_record")
@KeySequence("member_sign_in_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignInRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 签到用户
     */
    private Long userId;
    /**
     * 第几天签到
     */
    private Integer day;
    /**
     * 签到的积分
     */
    private Integer point;
    /**
     * 签到的经验
     */
    private Integer experience;

}
