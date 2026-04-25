package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskStepDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 服装设计流水线任务步骤 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionTaskStepMapper extends BaseMapperX<AiFashionTaskStepDO> {

    default List<AiFashionTaskStepDO> selectListByTaskId(Long taskId) {
        return selectList(new LambdaQueryWrapperX<AiFashionTaskStepDO>()
                .eq(AiFashionTaskStepDO::getTaskId, taskId)
                .orderByAsc(AiFashionTaskStepDO::getStepOrder));
    }

}
