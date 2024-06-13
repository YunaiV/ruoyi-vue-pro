package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    /**
     * 查询 - page
     *
     * @param userId
     * @param pageReqVO
     * @return
     */
    default PageResult<AiImageDO> selectPage(Long userId, PageParam pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AiImageDO>()
                .eq(AiImageDO::getUserId, userId)
                .orderByDesc(AiImageDO::getId));
    }

    /**
     * 查询 - 根据 status 和 platform
     *
     * @return
     */
    default List<AiImageDO> selectByStatusAndPlatform(AiImageStatusEnum statusEnum, AiPlatformEnum platformEnum) {
        return this.selectList(new LambdaUpdateWrapper<AiImageDO>()
                .eq(AiImageDO::getStatus, statusEnum.getStatus())
                .eq(AiImageDO::getPlatform, platformEnum.getPlatform())
        );
    }

}
