package cn.iocoder.yudao.module.ai1.dal.mysql.memory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI1 长期记忆 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1MemoryMapper extends BaseMapperX<Ai1MemoryDO> {

    default PageResult<Ai1MemoryDO> selectPage(Ai1MemoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1MemoryDO>()
                .eqIfPresent(Ai1MemoryDO::getAgentId, reqVO.getAgentId())
                .eqIfPresent(Ai1MemoryDO::getUserId, reqVO.getUserId())
                .orderByDesc(Ai1MemoryDO::getId));
    }

    /**
     * 未加唯一索引，极端并发下可能存在重复记录，取第一条
     */
    default Ai1MemoryDO selectByAgentIdAndUserId(Long agentId, Long userId) {
        return selectFirstOne(Ai1MemoryDO::getAgentId, agentId, Ai1MemoryDO::getUserId, userId);
    }

}
