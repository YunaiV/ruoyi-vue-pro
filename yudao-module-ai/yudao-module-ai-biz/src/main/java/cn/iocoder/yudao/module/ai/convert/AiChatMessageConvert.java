package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.vo.AiChatMessageListRes;
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
public interface AiChatMessageConvert {

    AiChatMessageConvert INSTANCE = Mappers.getMapper(AiChatMessageConvert.class);

    /**
     * 转换  ChatMessageListRes
     *
     * @param list
     * @return
     */
    List<AiChatMessageListRes> convert(List<AiChatMessageDO> list);
}
