package cn.iocoder.yudao.module.promotion.dal.mysql.combination.combinationactivity;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationRecordDO;
import org.apache.ibatis.annotations.Mapper;

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

}
