package cn.iocoder.yudao.module.ai.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * ai image
 *
 * @author fansili
 * @time 2024/4/28 14:01
 * @since 1.0
 */
@Repository
@Mapper
public interface AiImageMapper extends BaseMapperX<AiImageDO> {


}
