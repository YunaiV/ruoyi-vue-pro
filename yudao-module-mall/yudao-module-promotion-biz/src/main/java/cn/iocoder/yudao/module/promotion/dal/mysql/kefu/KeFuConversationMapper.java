package cn.iocoder.yudao.module.promotion.dal.mysql.kefu;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuConversationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客服会话 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface KeFuConversationMapper extends BaseMapperX<KeFuConversationDO> {

    // TODO @puhui999：排序可以交给前端，或者 controller；数据库的计算尽量少哈；
    default List<KeFuConversationDO> selectListWithSort() {
        return selectList(new LambdaQueryWrapperX<KeFuConversationDO>()
                .eq(KeFuConversationDO::getAdminDeleted, Boolean.FALSE)
                .orderByDesc(KeFuConversationDO::getAdminPinned) // 置顶优先
                .orderByDesc(KeFuConversationDO::getCreateTime));
    }

    // TODO @puhui999：是不是置零，用 update 就 ok 拉；然后单独搞个 +1 的方法；
    default void updateAdminUnreadMessageCountByConversationId(Long id, Integer count) {
        LambdaUpdateWrapper<KeFuConversationDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KeFuConversationDO::getId, id);
        if (count != null && count > 0) { // 情况一：会员发送消息时增加管理员的未读消息数
            updateWrapper.setSql("admin_unread_message_count = admin_unread_message_count + 1");
        } else { // 情况二：管理员已读后重置
            updateWrapper.set(KeFuConversationDO::getAdminUnreadMessageCount, 0);
        }
        update(updateWrapper);
    }

}