package cn.iocoder.yudao.module.ai.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dataobject.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * message mapper
 *
 * @fansili
 * @since v1.0
 */
@Repository
@Mapper
public interface AiChatMessageMapper extends BaseMapperX<AiChatMessage> {
}
