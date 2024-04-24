package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.vo.ChatRoleListRes;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 聊天 对话 convert
 *
 * @author fansili
 * @time 2024/4/18 16:39
 * @since 1.0
 */
@Mapper
public interface ChatRoleConvert {

    ChatRoleConvert INSTANCE = Mappers.getMapper(ChatRoleConvert.class);

    /**
     * 转换 - ChatRoleListRes
     *
     * @param roleList
     * @return
     */
    List<ChatRoleListRes> convertChatRoleListRes(List<AiChatRoleDO> roleList);
}
