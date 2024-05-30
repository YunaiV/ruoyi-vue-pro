package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * AI 绘图 Mapper
 *
 * @author fansili
 */
@Mapper
public interface AiImageMapper extends BaseMapperX<AiImageDO> {

    // TODO @fan：这个建议，直接使用 update，service 拼接要改的状态哈
    /**
     * 更新 - 根据 messageId
     *
     * @param mjNonceId
     * @param aiImageDO
     */
    default void updateByMjNonce(Long mjNonceId, AiImageDO aiImageDO) {
//        this.update(aiImageDO, new LambdaQueryWrapperX<AiImageDO>().eq(AiImageDO::getMjNonceId, mjNonceId));
        return;
    }

}
