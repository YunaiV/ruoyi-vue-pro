package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionModelLibraryPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionModelLibraryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 服装设计素材库 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionModelLibraryMapper extends BaseMapperX<AiFashionModelLibraryDO> {

    default PageResult<AiFashionModelLibraryDO> selectPage(AiFashionModelLibraryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiFashionModelLibraryDO>()
                .eqIfPresent(AiFashionModelLibraryDO::getSourceType, reqVO.getSourceType())
                .eqIfPresent(AiFashionModelLibraryDO::getCategory, reqVO.getCategory())
                .eqIfPresent(AiFashionModelLibraryDO::getBrand, reqVO.getBrand())
                .eqIfPresent(AiFashionModelLibraryDO::getIsModel, reqVO.getIsModel())
                .eqIfPresent(AiFashionModelLibraryDO::getModelPose, reqVO.getModelPose())
                .eqIfPresent(AiFashionModelLibraryDO::getModelBodyType, reqVO.getModelBodyType())
                .likeIfPresent(AiFashionModelLibraryDO::getTitle, reqVO.getKeyword())
                .betweenIfPresent(AiFashionModelLibraryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiFashionModelLibraryDO::getId));
    }

}
