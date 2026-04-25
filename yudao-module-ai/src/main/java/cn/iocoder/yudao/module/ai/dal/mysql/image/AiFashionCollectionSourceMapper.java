package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionCollectionSourceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 服装设计素材采集源 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionCollectionSourceMapper extends BaseMapperX<AiFashionCollectionSourceDO> {

    default List<AiFashionCollectionSourceDO> selectBySourceType(String sourceType) {
        return selectList(new LambdaQueryWrapperX<AiFashionCollectionSourceDO>()
                .eqIfPresent(AiFashionCollectionSourceDO::getSourceType, sourceType)
                .eq(AiFashionCollectionSourceDO::getStatus, "active")
                .orderByAsc(AiFashionCollectionSourceDO::getPriority));
    }

    default List<AiFashionCollectionSourceDO> selectAllActive() {
        return selectList(new LambdaQueryWrapperX<AiFashionCollectionSourceDO>()
                .eq(AiFashionCollectionSourceDO::getStatus, "active")
                .orderByAsc(AiFashionCollectionSourceDO::getPriority));
    }

}
