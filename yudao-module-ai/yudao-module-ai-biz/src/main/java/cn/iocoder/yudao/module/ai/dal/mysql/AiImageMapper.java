package cn.iocoder.yudao.module.ai.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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
@Mapper
public interface AiImageMapper extends BaseMapperX<AiImageDO> {


    /**
     * 更新 - 根据 messageId
     *
     * @param mjNonceId
     * @param aiImageDO
     */
    default void updateByMjNonce(Long mjNonceId, AiImageDO aiImageDO) {
        this.update(aiImageDO, new LambdaQueryWrapperX<AiImageDO>().eq(AiImageDO::getMjNonceId, mjNonceId));
    }
}
