package cn.iocoder.yudao.module.ai.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatModalDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * chat modal
 *
 * @author fansili
 * @time 2024/4/24 19:41
 * @since 1.0
 */
@Repository
@Mapper
public interface AiChatModalMapper extends BaseMapperX<AiChatModalDO> {
}
