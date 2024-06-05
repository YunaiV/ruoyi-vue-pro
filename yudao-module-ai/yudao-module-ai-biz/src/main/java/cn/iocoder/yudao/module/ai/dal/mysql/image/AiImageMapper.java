package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import org.apache.ibatis.annotations.Mapper;

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

    /**
     * 查询 - 根据 job id
     *
     * @param id
     * @return
     */
    default AiImageDO selectByJobId(String id) {
        return this.selectOne(new LambdaQueryWrapperX<AiImageDO>().eq(AiImageDO::getTaskId, id));
    }

    default PageResult<AiImageDO> selectPage(Long userId, PageParam pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AiImageDO>()
                .eq(AiImageDO::getUserId, userId)
                .orderByDesc(AiImageDO::getId));
    }

}
