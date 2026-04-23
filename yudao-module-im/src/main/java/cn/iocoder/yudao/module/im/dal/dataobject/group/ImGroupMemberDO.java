package cn.iocoder.yudao.module.im.dal.dataobject.group;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 群成员 DO
 *
 * @author 芋道源码
 */
@TableName("im_group_member")
@KeySequence("im_group_member_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupMemberDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 群编号
     * <p>
     * 关联 {@link ImGroupDO#getId()}
     */
    private Long groupId;
    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 组内显示名
     */
    private String displayUserName;
    /**
     * 群显示备注
     */
    private String displayGroupName;
    /**
     * 是否免打扰
     */
    private Boolean muted;
    /**
     * 成员状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 入群时间
     */
    private LocalDateTime joinTime;
    /**
     * 退群时间
     */
    private LocalDateTime quitTime;

}