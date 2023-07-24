package cn.iocoder.yudao.module.promotion.dal.mysql.combination.combinationactivity;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 拼团记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationRecordMapper extends BaseMapperX<CombinationRecordDO> {

    default CombinationRecordDO selectRecord(Long userId, Long orderId) {
        return selectOne(CombinationRecordDO::getUserId, userId, CombinationRecordDO::getOrderId, orderId);
    }

    default List<CombinationRecordDO> selectListByHeadIdAndStatus(Long headId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>().eq(CombinationRecordDO::getHeadId, headId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    default List<CombinationRecordDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>().eq(CombinationRecordDO::getStatus, status));
    }

}
