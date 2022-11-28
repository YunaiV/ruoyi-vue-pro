package cn.iocoder.yudao.module.promotion.dal.mysql.seckillactivity;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity.SeckillProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SeckillProductMapper extends BaseMapperX<SeckillProductDO> {

    default List<SeckillProductDO> selectListByActivityId(Long id){
        return selectList(SeckillProductDO::getActivityId,id);
    }

    default List<SeckillProductDO> selectListBySkuIds(Collection<Long> skuIds){
        return selectList(SeckillProductDO::getSkuId,skuIds);
    }

}
