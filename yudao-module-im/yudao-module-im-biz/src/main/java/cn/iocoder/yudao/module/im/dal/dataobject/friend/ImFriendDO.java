package cn.iocoder.yudao.module.im.dal.dataobject.friend;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @AI：好友关系是逻辑删除，群成员是 status 标识，感觉是不是要统一下？感觉都改成 status 会不会好点？这样增加一个添加时间、和删除时间；都使用 statusenum 标识；
/**
 * IM 好友关系 DO
 * <p>
 * 删除好友采用逻辑删除，沿用 BaseDO 的 deleted。
 * 重新添加好友时优先恢复原关系记录，不新插重复行。
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
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 好友用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long friendUserId;
    /**
     * 是否免打扰
     */
    private Boolean muted;

}
