package cn.iocoder.yudao.module.ai.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * chat modal
 *
 * @author fansili
 * @time 2024/4/24 19:41
 * @since 1.0
 */
@Repository
@Mapper
public interface AiChatModelMapper extends BaseMapperX<AiChatModelDO> {

    /**
     * 查询 - 第一个modal
     *
     * @return
     */
    default AiChatModelDO selectFirstModal() {
        PageResult<AiChatModelDO> pageResult = selectPage(new PageParam().setPageNo(1).setPageSize(1),
                new LambdaQueryWrapperX<AiChatModelDO>()
                        .orderByAsc(AiChatModelDO::getSort)
        );
        if (CollUtil.isEmpty(pageResult.getList())) {
            return null;
        }
        return pageResult.getList().get(0);
    }

    /**
     * 查询 - 根据 ids
     *
     * @param modalIds
     * @return
     */
    List<AiChatModelDO> selectByIds(Collection<Long> modalIds);
}
