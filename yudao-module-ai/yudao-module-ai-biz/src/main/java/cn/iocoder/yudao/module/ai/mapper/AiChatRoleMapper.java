package cn.iocoder.yudao.module.ai.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * role mapper
 *
 * @fansili
 * @since v1.0
 */
@Repository
@Mapper
public interface AiChatRoleMapper extends BaseMapperX<AiChatRoleDO> {
}
