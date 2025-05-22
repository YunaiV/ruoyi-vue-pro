package cn.iocoder.yudao.module.member.dal.dataobject.point;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 用户积分记录 DO
 *
 * @author QingX
 */
@TableName("member_point_record")
@KeySequence("member_point_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPointRecordDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 对应 MemberUserDO 的 id 属性
     */
    private Long userId;

    /**
     * 业务编码
     */
    private String bizId;
    /**
     * 业务类型
     *
     * 枚举 {@link MemberPointBizTypeEnum}
     */
    private Integer bizType;

    /**
     * 积分标题
     */
    private String title;
    /**
     * 积分描述
     */
    private String description;

    /**
     * 变动积分
     *
     * 1、正数表示获得积分
     * 2、负数表示消耗积分
     */
    private Integer point;
    /**
     * 变动后的积分
     */
    private Integer totalPoint;

}
