package cn.iocoder.yudao.module.statistics.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.statistics.controller.admin.product.vo.ProductStatisticsReqVO;
import cn.iocoder.yudao.module.statistics.controller.admin.product.vo.ProductStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.product.ProductStatisticsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface ProductStatisticsMapper extends BaseMapperX<ProductStatisticsDO> {

    default PageResult<ProductStatisticsDO> selectPageGroupBySpuId(ProductStatisticsReqVO reqVO, SortablePageParam pageParam) {
        return selectPage(pageParam, buildWrapper(reqVO)
                .groupBy(ProductStatisticsDO::getSpuId)
                .select(ProductStatisticsDO::getSpuId)
        );
    }

    default List<ProductStatisticsDO> selectListByTimeBetween(ProductStatisticsReqVO reqVO) {
        return selectList(buildWrapper(reqVO)
                .groupBy(ProductStatisticsDO::getTime)
                .select(ProductStatisticsDO::getTime));
    }

    default ProductStatisticsRespVO selectVoByTimeBetween(ProductStatisticsReqVO reqVO) {
        return selectJoinOne(ProductStatisticsRespVO.class, buildWrapper(reqVO));
    }

    /**
     * 构建 LambdaWrapper
     *
     * @param reqVO 查询参数
     * @return LambdaWrapper
     */
    private static MPJLambdaWrapperX<ProductStatisticsDO> buildWrapper(ProductStatisticsReqVO reqVO) {
        return new MPJLambdaWrapperX<ProductStatisticsDO>()
                .betweenIfPresent(ProductStatisticsDO::getTime, reqVO.getTimes())
                .selectSum(ProductStatisticsDO::getBrowseCount)
                .selectSum(ProductStatisticsDO::getBrowseUserCount)
                .selectSum(ProductStatisticsDO::getFavoriteCount)
                .selectSum(ProductStatisticsDO::getCartCount)
                .selectSum(ProductStatisticsDO::getOrderCount)
                .selectSum(ProductStatisticsDO::getOrderPayCount)
                .selectSum(ProductStatisticsDO::getOrderPayPrice)
                .selectSum(ProductStatisticsDO::getAfterSaleCount)
                .selectSum(ProductStatisticsDO::getAfterSaleRefundPrice)
                .selectAvg(ProductStatisticsDO::getBrowseConvertPercent);
    }

}