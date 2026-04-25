package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionModelFeaturesDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 服装设计模特特征 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionModelFeaturesMapper extends BaseMapperX<AiFashionModelFeaturesDO> {

    default AiFashionModelFeaturesDO selectByLibraryImageId(Long libraryImageId) {
        return selectOne(new LambdaQueryWrapperX<AiFashionModelFeaturesDO>()
                .eq(AiFashionModelFeaturesDO::getLibraryImageId, libraryImageId));
    }

    default List<AiFashionModelFeaturesDO> selectByPoseType(String poseType) {
        return selectList(new LambdaQueryWrapperX<AiFashionModelFeaturesDO>()
                .eqIfPresent(AiFashionModelFeaturesDO::getPoseType, poseType));
    }

}
