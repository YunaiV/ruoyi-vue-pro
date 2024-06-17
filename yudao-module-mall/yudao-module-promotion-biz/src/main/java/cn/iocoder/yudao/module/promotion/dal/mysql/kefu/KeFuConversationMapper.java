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

    default List<KeFuConversationDO> selectConversationList() {
        return selectList(new LambdaQueryWrapperX<KeFuConversationDO>()
                .eq(KeFuConversationDO::getAdminDeleted, Boolean.FALSE)
                .orderByDesc(KeFuConversationDO::getCreateTime));
    }

    // TODO @puhui999：这个不用单独搞个方法哈。Service 直接 new 一个对象，然后调用 update 方法。
    default void updateAdminUnreadMessageCountWithZero(Long id) {
        update(new LambdaUpdateWrapper<KeFuConversationDO>()
                .eq(KeFuConversationDO::getId, id)
                .set(KeFuConversationDO::getAdminUnreadMessageCount, 0));
    }

    // TODO @puhui999：改成 updateAdminUnreadMessageCountIncrement 增加
    default void updateAdminUnreadMessageCount(Long id) {
        update(new LambdaUpdateWrapper<KeFuConversationDO>()
                .eq(KeFuConversationDO::getId, id)
                .setSql("admin_unread_message_count = admin_unread_message_count + 1"));
    }

}