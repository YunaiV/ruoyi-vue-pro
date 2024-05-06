package cn.iocoder.yudao.module.ai.mapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * message mapper
 *
 * @fansili
 * @since v1.0
 */
@Repository
@Mapper
public interface AiChatConversationMapper extends BaseMapperX<AiChatConversationDO> {

    /**
     * 更新 - chat count
     *
     * @param id
     */
    @Update("update ai_chat_conversation set chat_count = chat_count + 1 where id = #{id}")
    void updateIncrChatCount(@Param("id") Long id);

    /**
     * 查询 - 最新的对话
     *
     * @param loginUserId
     */
    default AiChatConversationDO selectLatestConversation(Long loginUserId) {
        PageResult<AiChatConversationDO> pageResult = selectPage(new PageParam().setPageNo(1).setPageSize(1),
                new LambdaQueryWrapper<AiChatConversationDO>()
                        .eq(AiChatConversationDO::getUserId, loginUserId)
                        .orderByDesc(AiChatConversationDO::getId));
        if (CollUtil.isEmpty(pageResult.getList())) {
            return null;
        }
        return pageResult.getList().get(0);
    }

    /**
     * 查询 - 前100
     *
     * @param search
     */
    default List<AiChatConversationDO> selectTop100Conversation(Long loginUserId, String search) {
        LambdaQueryWrapper<AiChatConversationDO> queryWrapper
                = new LambdaQueryWrapper<AiChatConversationDO>().eq(AiChatConversationDO::getUserId, loginUserId);
        if (!StrUtil.isBlank(search)) {
            queryWrapper.like(AiChatConversationDO::getTitle, search);
        }
        queryWrapper.orderByDesc(AiChatConversationDO::getId);
        return selectPage(new PageParam().setPageNo(1).setPageSize(100), queryWrapper).getList();
    }
}
