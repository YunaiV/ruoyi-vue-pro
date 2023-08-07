package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 砍价商品 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface BargainProductMapper extends BaseMapperX<BargainProductDO> {

    default List<BargainProductDO> selectListByActivityIds(Collection<Long> ids) {
        return selectList(BargainProductDO::getActivityId, ids);
    }

}
