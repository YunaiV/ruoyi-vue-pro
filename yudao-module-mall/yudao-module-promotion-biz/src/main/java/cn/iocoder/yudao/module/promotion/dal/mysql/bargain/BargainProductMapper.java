package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product.BargainProductPageReqVO;
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

    default PageResult<BargainProductDO> selectPage(BargainProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BargainProductDO>()
                .eqIfPresent(BargainProductDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(BargainProductDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(BargainProductDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(BargainProductDO::getActivityStatus, reqVO.getActivityStatus())
                .betweenIfPresent(BargainProductDO::getActivityStartTime, reqVO.getActivityStartTime())
                .betweenIfPresent(BargainProductDO::getActivityEndTime, reqVO.getActivityEndTime())
                .betweenIfPresent(BargainProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BargainProductDO::getId));
    }

    default List<BargainProductDO> selectListByActivityIds(Collection<Long> ids) {
        return selectList(BargainProductDO::getActivityId, ids);
    }

}
