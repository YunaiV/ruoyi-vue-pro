package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatModalDO;
import cn.iocoder.yudao.module.ai.vo.AiChatModalListRes;
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
public interface ChatModalConvert {

    ChatModalConvert INSTANCE = Mappers.getMapper(ChatModalConvert.class);


    List<AiChatModalListRes> convertAiChatModalListRes(List<AiChatModalDO> list);
}
