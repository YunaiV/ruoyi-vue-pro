package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 秒杀活动商品 Mapper
 *
 * @author halfninety
 */
@Mapper
public interface SeckillProductMapper extends BaseMapperX<SeckillProductDO> {

    default List<SeckillProductDO> selectListByActivityId(Long id) {
        return selectList(SeckillProductDO::getActivityId, id);
    }

    default List<SeckillProductDO> selectListBySkuIds(Collection<Long> skuIds) {
        return selectList(SeckillProductDO::getSkuId, skuIds);
    }

    default void updateTimeIdsByActivityId(Long id, List<Long> timeIds) {
        new LambdaUpdateChainWrapper<>(this)
                .set(SeckillProductDO::getTimeIds, CollUtil.join(timeIds, ","))
                .eq(SeckillProductDO::getActivityId, id)
                .update();
    }

}
