package cn.iocoder.yudao.module.pay.dal.mysql.blockchain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.blockchain.BlockchainTaskDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 区块链存证任务 Mapper
 *
 * @author deepay
 */
@Mapper
public interface BlockchainTaskMapper extends BaseMapperX<BlockchainTaskDO> {

    /**
     * 根据任务 ID 查询任务
     */
    default BlockchainTaskDO selectByTaskId(String taskId) {
        return selectOne(new LambdaQueryWrapper<BlockchainTaskDO>()
                .eq(BlockchainTaskDO::getTaskId, taskId));
    }

    /**
     * 根据订单号查询任务列表
     */
    default List<BlockchainTaskDO> selectListByOrderId(String orderId) {
        return selectList(new LambdaQueryWrapper<BlockchainTaskDO>()
                .eq(BlockchainTaskDO::getOrderId, orderId)
                .orderByDesc(BlockchainTaskDO::getId));
    }

    /**
     * 查询指定状态的任务列表（供定时任务补偿使用）
     */
    default List<BlockchainTaskDO> selectListByStatus(String status) {
        return selectList(new LambdaQueryWrapper<BlockchainTaskDO>()
                .eq(BlockchainTaskDO::getStatus, status)
                .orderByAsc(BlockchainTaskDO::getId));
    }

}
