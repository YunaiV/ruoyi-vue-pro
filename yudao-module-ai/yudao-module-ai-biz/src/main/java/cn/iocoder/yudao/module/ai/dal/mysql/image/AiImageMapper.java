package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 绘图 Mapper
 *
 * @author fansili
 */
@Mapper
public interface AiImageMapper extends BaseMapperX<AiImageDO> {

    default AiImageDO selectByTaskId(String taskId) {
        return this.selectOne(AiImageDO::getTaskId, taskId);
    }

    default PageResult<AiImageDO> selectPage(Long userId, PageParam pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AiImageDO>()
                .eq(AiImageDO::getUserId, userId)
                .orderByDesc(AiImageDO::getId));
    }

    default List<AiImageDO> selectListByStatusAndPlatform(Integer status, String platform) {
        return selectList(AiImageDO::getStatus, status,
                AiImageDO::getPlatform, platform);
    }

}
