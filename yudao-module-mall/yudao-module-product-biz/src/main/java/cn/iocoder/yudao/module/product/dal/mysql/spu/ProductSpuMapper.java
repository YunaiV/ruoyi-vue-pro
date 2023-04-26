package cn.iocoder.yudao.module.product.dal.mysql.spu;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Objects;
import java.util.Set;

/**
 * 商品spu Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSpuMapper extends BaseMapperX<ProductSpuDO> {

    default PageResult<ProductSpuDO> selectPage(ProductSpuPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductSpuDO::getStatus, reqVO.getStatus())
                .leIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMax())
                .geIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMin())
                .leIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMax())
                .geIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMin())
                .orderByDesc(ProductSpuDO::getSort));
    }

    default PageResult<ProductSpuDO> selectPage(ProductSpuPageReqVO reqVO, Set<Long> alarmStockSpuIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductSpuDO::getStatus, reqVO.getStatus())
                .leIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMax())
                .geIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMin())
                .leIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMax())
                .geIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMin())
                .inIfPresent(ProductSpuDO::getId, alarmStockSpuIds) // 库存告警
                .eqIfPresent(ProductSpuDO::getStatus, reqVO.getStatus())
                .orderByDesc(ProductSpuDO::getSort));
    }

    /**
     * 获得商品 SPU 分页，提供给用户 App 使用
     */
    default PageResult<ProductSpuDO> selectPage(AppProductSpuPageReqVO pageReqVO, Set<Long> categoryIds) {
        LambdaQueryWrapperX<ProductSpuDO> query = new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, pageReqVO.getKeyword()) // 关键字匹配，目前只匹配商品名
                .inIfPresent(ProductSpuDO::getCategoryId, categoryIds); // 分类
        query.eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus()) // 上架状态
                .gt(ProductSpuDO::getStock, 0); // 有库存
        // 推荐类型的过滤条件
        if (ObjUtil.equal(pageReqVO.getRecommendType(), AppProductSpuPageReqVO.RECOMMEND_TYPE_HOT)) {
            query.eq(ProductSpuDO::getRecommendHot, true);
        }
        // 排序逻辑
        if (Objects.equals(pageReqVO.getSortField(), AppProductSpuPageReqVO.SORT_FIELD_SALES_COUNT)) {
            query.last(String.format(" ORDER BY (sales_count + virtual_sales_count) %s, sort DESC, id DESC",
                    pageReqVO.getSortAsc() ? "ASC" : "DESC"));
        } else if (Objects.equals(pageReqVO.getSortField(), AppProductSpuPageReqVO.SORT_FIELD_PRICE)) {
            query.orderBy(true, pageReqVO.getSortAsc(), ProductSpuDO::getPrice)
                    .orderByDesc(ProductSpuDO::getSort).orderByDesc(ProductSpuDO::getId);
        } else {
            query.orderByDesc(ProductSpuDO::getSort).orderByDesc(ProductSpuDO::getId);
        }
        return selectPage(pageReqVO, query);
    }

    /**
     * 更新商品 SPU 库存
     *
     * @param id 商品 SPU 编号
     * @param incrCount 增加的库存数量
     */
    default void updateStock(Long id, Integer incrCount) {
        LambdaUpdateWrapper<ProductSpuDO> updateWrapper = new LambdaUpdateWrapper<ProductSpuDO>()
                .setSql(" total_stock = total_stock +" + incrCount) // 负数，所以使用 + 号
                .eq(ProductSpuDO::getId, id);
        update(null, updateWrapper);
    }

}
