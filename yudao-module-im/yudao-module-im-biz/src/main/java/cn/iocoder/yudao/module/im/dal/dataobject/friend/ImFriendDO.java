package cn.iocoder.yudao.module.im.dal.dataobject.friend;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IM 好友关系 DO
 * <p>
 * 业务语义：
 * - 双向关系：A-B 互为好友会存 2 条记录（userId=A, friendUserId=B 和 userId=B, friendUserId=A）
 * - 软删除：`deleted=1` 表示单向解除好友；如果需要"全部不看到"，双向都要 deleted=1
 * - 免打扰：`muted` 控制是否屏蔽来自该好友的通知
 *
 * @author 芋道源码
 */
@TableName("im_friend")
@KeySequence("im_friend_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImFriendDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 好友用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long friendUserId;
    /**
     * 是否免打扰
     */
    private Boolean muted;

    // TODO @AI：status 字段，枚举 CommonStatusEnum，表示正常 / 解散；如果是解散状态，则不允许任何操作了。
    // TODO @AI：addTime、deleteTime 字段，分别表示添加好友的时间和删除好友的时间；如果 deleteTime 不为 null，则表示已删除。不使用 deleted；

    // 说明：
    // - 删除状态用 BaseDO 的 deleted 字段表达（0=正常，1=已删除）
    // - 添加时间用 BaseDO 的 createTime；删除时间对应 updateTime

}
