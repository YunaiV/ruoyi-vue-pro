package cn.iocoder.yudao.module.im.dal.dataobject.group;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IM 群信息 DO
 *
 * @author 芋道源码
 */
@TableName("im_group")
@KeySequence("im_group_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 群名称
     */
    private String name;
    /**
     * 群主用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;
    /**
     * 群头像
     */
    private String avatar;
    /**
     * 群公告
     */
    private String notice;
    /**
     * 是否封禁
     */
    private Boolean banned;
    /**
     * 封禁原因
     */
    private String bannedReason;
    // TODO @AI：封禁时间；
    /**
     * 是否解散
     */
    private Boolean dissolved;
    // TODO @AI：解散时间

}