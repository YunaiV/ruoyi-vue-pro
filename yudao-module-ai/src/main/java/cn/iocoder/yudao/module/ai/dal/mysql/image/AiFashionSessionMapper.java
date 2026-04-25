package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI 服装设计会话 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionSessionMapper extends BaseMapperX<AiFashionSessionDO> {

    @Select("SELECT * FROM ai_fashion_session WHERE user_id = #{userId} AND session_token = #{token} AND deleted = 0 LIMIT 1")
    AiFashionSessionDO selectByUserAndToken(@Param("userId") Long userId, @Param("token") String token);

}
