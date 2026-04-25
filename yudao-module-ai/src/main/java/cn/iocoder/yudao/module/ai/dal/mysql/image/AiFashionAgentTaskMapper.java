package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionAgentTaskDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 服装设计智能体链路任务 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionAgentTaskMapper extends BaseMapperX<AiFashionAgentTaskDO> {

    /** 按链路编号查询所有步骤，按 stepOrder 升序 */
    default List<AiFashionAgentTaskDO> selectByChainId(String chainId) {
        return selectList(new LambdaQueryWrapper<AiFashionAgentTaskDO>()
                .eq(AiFashionAgentTaskDO::getChainId, chainId)
                .orderByAsc(AiFashionAgentTaskDO::getStepOrder));
    }

    /** 查询链路中指定步骤 */
    default AiFashionAgentTaskDO selectByChainStep(String chainId, int stepOrder) {
        return selectOne(new LambdaQueryWrapper<AiFashionAgentTaskDO>()
                .eq(AiFashionAgentTaskDO::getChainId, chainId)
                .eq(AiFashionAgentTaskDO::getStepOrder, stepOrder));
    }

    /** 查询用户最近 N 条链路（取每条链路的第一步代表该链路） */
    default List<AiFashionAgentTaskDO> selectRecentChainHeadsByUser(Long userId, int limit) {
        return selectList(new LambdaQueryWrapper<AiFashionAgentTaskDO>()
                .eq(AiFashionAgentTaskDO::getUserId, userId)
                .eq(AiFashionAgentTaskDO::getStepOrder, 0)
                .orderByDesc(AiFashionAgentTaskDO::getId)
                .last("LIMIT " + limit));
    }

}
