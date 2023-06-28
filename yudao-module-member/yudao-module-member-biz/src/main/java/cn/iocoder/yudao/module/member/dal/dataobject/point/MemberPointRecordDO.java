package cn.iocoder.yudao.module.member.dal.dataobject.point;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

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
     * 业务编码
     */
    private String bizId;
    /**
     * 业务类型
     *
     * 枚举 {@link TODO biz_type 对应的类}
     */
    private String bizType;
    /**
     * 1增加 0扣减
     */
    private String type;
    /**
     * 积分标题
     */
    private String title;
    /**
     * 积分描述
     */
    private String description;
    /**
     * 积分
     */
    private Integer point;
    /**
     * 变动后的积分
     */
    private Integer totalPoint;
    /**
     * 状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款）

     *
     * 枚举 {@link TODO point_status 对应的类}
     */
    private Integer status;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 冻结时间
     */
    private LocalDateTime freezingTime;
    /**
     * 解冻时间
     */
    private LocalDateTime thawingTime;

}
