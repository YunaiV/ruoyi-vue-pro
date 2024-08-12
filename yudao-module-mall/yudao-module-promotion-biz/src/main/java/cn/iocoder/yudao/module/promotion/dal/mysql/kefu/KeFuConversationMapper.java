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

    default void updateAdminUnreadMessageCountIncrement(Long id) {
        update(new LambdaUpdateWrapper<KeFuConversationDO>()
                .eq(KeFuConversationDO::getId, id)
                .setSql("admin_unread_message_count = admin_unread_message_count + 1"));
    }

    default KeFuConversationDO selectByUserId(Long userId) {
        return selectOne(KeFuConversationDO::getUserId, userId);
    }

}