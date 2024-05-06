package cn.iocoder.yudao.module.ai.mapper;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * chat modal
 *
 * @author fansili
 * @time 2024/4/24 19:41
 * @since 1.0
 */
@Repository
@Mapper
public interface AiChatModalMapper extends BaseMapperX<AiChatModalDO> {

    /**
     * 查询 - 第一个modal
     *
     * @return
     */
    default AiChatModalDO selectFirstModal() {
        PageResult<AiChatModalDO> pageResult = selectPage(new PageParam().setPageNo(1).setPageSize(1),
                new LambdaQueryWrapperX<AiChatModalDO>()
                        .orderByAsc(AiChatModalDO::getSort)
        );
        if (CollUtil.isEmpty(pageResult.getList())) {
            return null;
        }
        return pageResult.getList().get(0);
    }


}
